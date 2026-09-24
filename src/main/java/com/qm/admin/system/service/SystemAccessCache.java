package com.qm.admin.system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qm.admin.system.config.SystemAccessCacheProperties;
import com.qm.admin.system.dto.MenuResponse;
import com.qm.admin.system.dto.UserInfoResponse;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 版本化业务缓存。Lua 将三级版本和业务数据读取合并为一次 Redis 往返；缓存未命中时，
 * Redisson 分布式单飞锁保证多实例只有一个请求回源数据库。失效只递增版本，旧请求只能回填旧 key。
 */
@Component
public class SystemAccessCache {

    private static final Logger log = LoggerFactory.getLogger(SystemAccessCache.class);
    private static final String GLOBAL_VERSION = "qm:access:version:global";
    private static final String TENANT_VERSION = "qm:access:version:tenant:";
    private static final String USER_VERSION = "qm:access:version:user:";
    private static final String CACHE_PREFIX = "qm:access:";
    private static final String LOAD_LOCK_PREFIX = "qm:access:load-lock:";
    private static final DefaultRedisScript<String> LOOKUP_SCRIPT = new DefaultRedisScript<>("""
            local globalVersion = redis.call('GET', KEYS[1]) or '0'
            local tenantVersion = redis.call('GET', KEYS[2]) or '0'
            local userVersion = redis.call('GET', KEYS[3]) or '0'
            local cacheKey = ARGV[1] .. ':' .. globalVersion .. ':' .. tenantVersion .. ':'
                .. userVersion .. ':' .. ARGV[2] .. ':' .. ARGV[3]
            local value = redis.call('GET', cacheKey)
            if value then
                return cjson.encode({key = cacheKey, value = value})
            end
            return cjson.encode({key = cacheKey})
            """, String.class);

    private final StringRedisTemplate redis;
    private final RedissonClient redisson;
    private final ObjectMapper objectMapper;
    private final SystemAccessCacheProperties properties;

    public SystemAccessCache(StringRedisTemplate redis, @Lazy RedissonClient redisson, ObjectMapper objectMapper,
                             SystemAccessCacheProperties properties) {
        this.redis = redis;
        this.redisson = redisson;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public List<String> getOrLoadPermissions(Long tenantId, Long userId, Supplier<List<String>> loader) {
        return getOrLoad("permissions", tenantId, userId, properties.getPermissionTtl(),
                new TypeReference<>() {}, loader);
    }

    public List<MenuResponse> getOrLoadMenus(Long tenantId, Long userId, Supplier<List<MenuResponse>> loader) {
        return getOrLoad("menus", tenantId, userId, properties.getMenuTtl(),
                new TypeReference<>() {}, loader);
    }

    public UserInfoResponse getOrLoadUserInfo(Long tenantId, Long userId, Supplier<UserInfoResponse> loader) {
        return getOrLoad("user-info", tenantId, userId, properties.getUserInfoTtl(),
                new TypeReference<>() {}, loader);
    }

    public void evictUser(Long tenantId, Long userId) { increment(USER_VERSION + tenantId + ":" + userId); }
    public void evictTenant(Long tenantId) { increment(TENANT_VERSION + tenantId); }
    public void evictAll() { increment(GLOBAL_VERSION); }

    private <T> T getOrLoad(String type, Long tenantId, Long userId, Duration ttl,
                            TypeReference<T> typeReference, Supplier<T> loader) {
        if (!properties.isEnabled() || tenantId == null || userId == null) return loader.get();
        CacheLookup firstLookup = lookup(type, tenantId, userId);
        if (firstLookup == null) return loader.get();
        T cachedValue = deserialize(firstLookup.value(), typeReference);
        if (cachedValue != null) return cachedValue;
        return loadWithLock(type, tenantId, userId, ttl, typeReference, loader, firstLookup.key());
    }

    private <T> T loadWithLock(String type, Long tenantId, Long userId, Duration ttl,
                               TypeReference<T> typeReference, Supplier<T> loader, String fallbackKey) {
        RLock lock;
        boolean acquired;
        try {
            lock = redisson.getLock(loadLockKey(type, tenantId, userId));
            acquired = lock.tryLock(properties.getLoadLockWaitTime().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for access cache load lock; falling back to database");
            return loadAndWrite(fallbackKey, ttl, loader);
        } catch (RuntimeException exception) {
            log.warn("Access cache load lock unavailable; falling back to database", exception);
            return loadAndWrite(fallbackKey, ttl, loader);
        }

        if (!acquired) {
            CacheLookup lastLookup = lookup(type, tenantId, userId);
            T cachedValue = lastLookup == null ? null : deserialize(lastLookup.value(), typeReference);
            return cachedValue == null
                    ? loadAndWrite(lastLookup == null ? fallbackKey : lastLookup.key(), ttl, loader)
                    : cachedValue;
        }

        try {
            // 获锁后必须二次检查，前一个持锁请求可能已经完成回填。
            CacheLookup secondLookup = lookup(type, tenantId, userId);
            if (secondLookup == null) return loader.get();
            T cachedValue = deserialize(secondLookup.value(), typeReference);
            return cachedValue == null
                    ? loadAndWrite(secondLookup.key(), ttl, loader)
                    : cachedValue;
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) lock.unlock();
            } catch (RuntimeException exception) {
                log.warn("Access cache load lock release failed; watchdog will expire it", exception);
            }
        }
    }

    private <T> T loadAndWrite(String cacheKey, Duration ttl, Supplier<T> loader) {
        T value = loader.get();
        write(cacheKey, value, ttl);
        return value;
    }

    private CacheLookup lookup(String type, Long tenantId, Long userId) {
        try {
            String result = redis.execute(LOOKUP_SCRIPT,
                    List.of(GLOBAL_VERSION, TENANT_VERSION + tenantId, USER_VERSION + tenantId + ":" + userId),
                    CACHE_PREFIX + type, String.valueOf(tenantId), String.valueOf(userId));
            return result == null ? null : objectMapper.readValue(result, CacheLookup.class);
        } catch (JsonProcessingException exception) {
            log.warn("Invalid access cache lookup result; falling back to database", exception);
            return null;
        } catch (RuntimeException exception) {
            log.warn("Redis access cache lookup failed; falling back to database", exception);
            return null;
        }
    }

    private <T> T deserialize(String value, TypeReference<T> typeReference) {
        if (value == null) return null;
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (JsonProcessingException exception) {
            log.warn("Invalid access cache payload; reloading from database", exception);
            return null;
        }
    }

    private void write(String key, Object value, Duration ttl) {
        if (key == null || value == null || ttl == null || ttl.isZero() || ttl.isNegative()) return;
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException | RuntimeException exception) {
            log.warn("Redis access cache write failed; database remains source of truth", exception);
        }
    }

    private void increment(String key) {
        if (!properties.isEnabled()) return;
        try { redis.opsForValue().increment(key); }
        catch (RuntimeException exception) { log.warn("Redis access cache invalidation failed", exception); }
    }

    private String loadLockKey(String type, Long tenantId, Long userId) {
        return LOAD_LOCK_PREFIX + type + ":" + tenantId + ":" + userId;
    }

    private record CacheLookup(String key, String value) {}
}
