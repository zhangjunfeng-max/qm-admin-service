package com.qm.admin.system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qm.admin.system.config.SystemAuthProperties;
import com.qm.admin.system.security.SystemPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * 访问令牌会话缓存。Redis 只保存令牌摘要对应的稳定主体，不保存原始令牌或密码。
 * Redis 异常时自动回退数据库认证，避免缓存基础设施故障扩大为登录系统不可用。
 */
@Component
public class AuthSessionCache {

    private static final Logger log = LoggerFactory.getLogger(AuthSessionCache.class);
    private static final String KEY_PREFIX = "qm:auth:access:";

    private final StringRedisTemplate redisTemplate;
    private final SystemAuthProperties authProperties;
    private final ObjectMapper objectMapper;

    public AuthSessionCache(StringRedisTemplate redisTemplate, SystemAuthProperties authProperties,
                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
        this.objectMapper = objectMapper;
    }

    public Optional<SystemPrincipal> get(String accessTokenHash) {
        if (!authProperties.isAccessTokenCacheEnabled()) {
            return Optional.empty();
        }
        try {
            String value = redisTemplate.opsForValue().get(key(accessTokenHash));
            if (value == null) {
                return Optional.empty();
            }
            CachedPrincipal cached = objectMapper.readValue(value, CachedPrincipal.class);
            return Optional.of(new SystemPrincipal(cached.userId(), cached.tenantId(), cached.username()));
        } catch (RuntimeException exception) {
            log.warn("Redis access token cache read failed; falling back to database authentication", exception);
            return Optional.empty();
        } catch (JsonProcessingException exception) {
            log.warn("Invalid access token cache payload; falling back to database authentication", exception);
            evict(accessTokenHash);
            return Optional.empty();
        }
    }

    public void put(String accessTokenHash, SystemPrincipal principal, LocalDateTime accessExpiresTime) {
        if (!authProperties.isAccessTokenCacheEnabled()) {
            return;
        }
        Duration untilTokenExpires = Duration.between(LocalDateTime.now(), accessExpiresTime);
        Duration ttl = minPositive(untilTokenExpires, authProperties.getAccessTokenCacheTtl());
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }
        try {
            String value = objectMapper.writeValueAsString(new CachedPrincipal(
                    principal.userId(), principal.tenantId(), principal.username()));
            redisTemplate.opsForValue().set(key(accessTokenHash), value, ttl);
        } catch (RuntimeException | JsonProcessingException exception) {
            log.warn("Redis access token cache write failed; database authentication remains available", exception);
        }
    }

    public void evict(String accessTokenHash) {
        if (!authProperties.isAccessTokenCacheEnabled() || accessTokenHash == null) {
            return;
        }
        try {
            redisTemplate.delete(key(accessTokenHash));
        } catch (RuntimeException exception) {
            log.warn("Redis access token cache eviction failed", exception);
        }
    }

    public void evictAll(Iterable<String> accessTokenHashes) {
        if (!authProperties.isAccessTokenCacheEnabled() || accessTokenHashes == null) {
            return;
        }
        Set<String> keys = new java.util.HashSet<>();
        accessTokenHashes.forEach(hash -> {
            if (hash != null) {
                keys.add(key(hash));
            }
        });
        if (keys.isEmpty()) {
            return;
        }
        try {
            redisTemplate.delete(keys);
        } catch (RuntimeException exception) {
            log.warn("Redis access token cache batch eviction failed", exception);
        }
    }

    private Duration minPositive(Duration first, Duration second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    private String key(String accessTokenHash) {
        return KEY_PREFIX + accessTokenHash;
    }

    private record CachedPrincipal(Long userId, Long tenantId, String username) {
    }
}
