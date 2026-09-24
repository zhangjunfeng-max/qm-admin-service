package com.qm.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qm.admin.system.config.SystemAuthProperties;
import com.qm.admin.system.security.SystemPrincipal;
import com.qm.admin.system.service.AuthSessionCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthSessionCacheTests {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private AuthSessionCache cache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        SystemAuthProperties properties = new SystemAuthProperties();
        properties.setAccessTokenCacheEnabled(true);
        properties.setAccessTokenCacheTtl(Duration.ofMinutes(5));
        cache = new AuthSessionCache(redisTemplate, properties, new ObjectMapper());
    }

    @Test
    void writesAndReadsHashedAccessTokenSession() {
        SystemPrincipal principal = new SystemPrincipal(7L, 11L, "user:with:separator");
        cache.put("token-hash", principal, LocalDateTime.now().plusHours(1));

        verify(valueOperations).set(eq("qm:auth:access:token-hash"), any(String.class),
                eq(Duration.ofMinutes(5)));

        when(valueOperations.get("qm:auth:access:token-hash"))
                .thenReturn("{\"userId\":7,\"tenantId\":11,\"username\":\"user:with:separator\"}");
        assertEquals(principal, cache.get("token-hash").orElseThrow());
    }

    @Test
    void fallsBackToDatabaseWhenRedisFails() {
        when(valueOperations.get("qm:auth:access:token-hash"))
                .thenThrow(new IllegalStateException("redis unavailable"));

        assertTrue(cache.get("token-hash").isEmpty());

        doThrow(new IllegalStateException("redis unavailable"))
                .when(redisTemplate).delete("qm:auth:access:token-hash");
        cache.evict("token-hash");
    }
}
