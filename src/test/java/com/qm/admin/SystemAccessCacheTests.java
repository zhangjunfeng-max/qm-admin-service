package com.qm.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qm.admin.system.config.SystemAccessCacheProperties;
import com.qm.admin.system.service.SystemAccessCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemAccessCacheTests {

    private final Map<String, String> values = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private StringRedisTemplate redis;
    private ValueOperations<String, String> operations;
    private RedissonClient redisson;
    private RLock lock;
    private SystemAccessCache cache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws InterruptedException {
        redis = mock(StringRedisTemplate.class);
        operations = mock(ValueOperations.class);
        redisson = mock(RedissonClient.class);
        lock = mock(RLock.class);
        when(redis.opsForValue()).thenReturn(operations);
        when(redis.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenAnswer(invocation -> lookup(invocation.getArgument(1), invocation.getArgument(2),
                        invocation.getArgument(3), invocation.getArgument(4)));
        doAnswer(invocation -> {
            values.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(operations).set(any(String.class), any(String.class), any(Duration.class));
        when(operations.increment(any(String.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            long next = Long.parseLong(values.getOrDefault(key, "0")) + 1;
            values.put(key, String.valueOf(next));
            return next;
        });
        when(redisson.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock(any(Long.class), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        cache = new SystemAccessCache(redis, redisson, objectMapper, new SystemAccessCacheProperties());
    }

    @Test
    void cacheHitUsesOneLuaLookupAndDoesNotQueryDatabaseAgain() {
        AtomicInteger loads = new AtomicInteger();
        List<String> first = cache.getOrLoadPermissions(1L, 7L, () -> {
            loads.incrementAndGet();
            return List.of("system:user:view");
        });
        List<String> second = cache.getOrLoadPermissions(1L, 7L, () -> {
            loads.incrementAndGet();
            return List.of("unexpected");
        });

        assertEquals(first, second);
        assertEquals(1, loads.get());
    }

    @Test
    void userInvalidationMovesReadsToNewVersionKey() {
        cache.getOrLoadPermissions(1L, 7L, () -> List.of("old"));
        cache.evictUser(1L, 7L);

        assertEquals(List.of("new"), cache.getOrLoadPermissions(1L, 7L, () -> List.of("new")));
        verify(operations).increment(eq("qm:access:version:user:1:7"));
    }

    @Test
    void staleDatabaseSnapshotCannotBeReadAfterConcurrentInvalidation() {
        List<String> stale = cache.getOrLoadPermissions(1L, 7L, () -> {
            cache.evictUser(1L, 7L);
            return List.of("old");
        });

        assertEquals(List.of("old"), stale);
        assertEquals(List.of("new"), cache.getOrLoadPermissions(1L, 7L, () -> List.of("new")));
    }

    @Test
    void redisFailureFallsBackToDatabaseWithoutTryingDistributedLock() {
        when(redis.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenThrow(new IllegalStateException("redis down"));

        assertEquals(List.of("database"), cache.getOrLoadPermissions(1L, 7L, () -> List.of("database")));
        verify(redisson, never()).getLock(any(String.class));
    }

    @Test
    void distributedLockPerformsSecondLookupBeforeDatabaseLoad() throws InterruptedException {
        AtomicInteger lookups = new AtomicInteger();
        when(redis.execute(any(RedisScript.class), anyList(), any(), any(), any())).thenAnswer(invocation -> {
            if (lookups.incrementAndGet() > 1) {
                values.put("qm:access:permissions:0:0:0:1:7", "[\"filled-by-other-node\"]");
            }
            return lookup(invocation.getArgument(1), invocation.getArgument(2),
                    invocation.getArgument(3), invocation.getArgument(4));
        });
        AtomicInteger loads = new AtomicInteger();

        List<String> result = cache.getOrLoadPermissions(1L, 7L, () -> {
            loads.incrementAndGet();
            return List.of("database");
        });

        assertEquals(List.of("filled-by-other-node"), result);
        assertEquals(0, loads.get());
        verify(lock).unlock();
    }

    @Test
    void lockTimeoutKeepsRequestAvailableAndLoadsDatabaseOnce() throws InterruptedException {
        when(lock.tryLock(any(Long.class), eq(TimeUnit.MILLISECONDS))).thenReturn(false);
        AtomicInteger loads = new AtomicInteger();

        List<String> result = cache.getOrLoadPermissions(1L, 7L, () -> {
            loads.incrementAndGet();
            return List.of("database");
        });

        assertEquals(List.of("database"), result);
        assertEquals(1, loads.get());
        verify(lock, never()).unlock();
    }

    private String lookup(List<String> keys, String prefix, String tenantId, String userId) throws Exception {
        String cacheKey = prefix + ":" + values.getOrDefault(keys.get(0), "0") + ":"
                + values.getOrDefault(keys.get(1), "0") + ":" + values.getOrDefault(keys.get(2), "0")
                + ":" + tenantId + ":" + userId;
        Map<String, String> result = new HashMap<>();
        result.put("key", cacheKey);
        if (values.containsKey(cacheKey)) result.put("value", values.get(cacheKey));
        return objectMapper.writeValueAsString(result);
    }
}
