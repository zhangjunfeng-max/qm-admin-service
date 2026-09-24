package com.qm.admin.config.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;

/**
 * Redisson 只承担分布式锁，不接管 Spring Data Redis 的连接工厂。
 * 空密码必须保持未配置状态，否则 Redisson 会向无密码 Redis 发送 AUTH 并导致应用启动失败。
 */
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    @Lazy
    public RedissonClient redissonClient(Config redissonConfiguration) {
        return Redisson.create(redissonConfiguration);
    }

    @Bean
    public Config redissonConfiguration(RedisProperties properties) {
        String scheme = properties.getSsl().isEnabled() ? "rediss://" : "redis://";
        Config config = new Config();
        SingleServerConfig server = config.useSingleServer()
                .setAddress(scheme + properties.getHost() + ":" + properties.getPort())
                .setDatabase(properties.getDatabase());
        if (properties.getConnectTimeout() != null) {
            server.setConnectTimeout(Math.toIntExact(properties.getConnectTimeout().toMillis()));
        }
        if (properties.getTimeout() != null) {
            server.setTimeout(Math.toIntExact(properties.getTimeout().toMillis()));
        }
        if (StringUtils.hasText(properties.getUsername())) {
            server.setUsername(properties.getUsername());
        }
        if (StringUtils.hasText(properties.getPassword())) {
            server.setPassword(properties.getPassword());
        }
        return config;
    }
}
