package com.qm.admin;

import com.qm.admin.config.redis.RedissonConfig;
import org.junit.jupiter.api.Test;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RedissonConfigTests {

    @Test
    void leavesPasswordUnsetWhenRedisPasswordIsBlank() throws Exception {
        RedisProperties properties = new RedisProperties();
        properties.setHost("127.0.0.1");
        properties.setPort(1);
        properties.setPassword("");

        Config config = new RedissonConfig().redissonConfiguration(properties);

        assertFalse(config.toYAML().contains("password:"));
    }
}
