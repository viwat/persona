package com.example.persona.location.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class LocationRedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    // How long to wait for a Redis command response.
    // Keep short: cache is best-effort; slow Redis should be skipped, not waited on.
    @Value("${spring.data.redis.timeout:500}")
    private int socketTimeoutMs;

    // How long jedisPool.getResource() blocks when the pool is exhausted before throwing.
    // Must be set explicitly here — the yml max-wait is read by Spring auto-config
    // but has no effect on a manually constructed JedisPool.
    @Value("${spring.data.redis.jedis.pool.max-wait-ms:500}")
    private long maxWaitMs;

    @Value("${spring.data.redis.jedis.pool.max-active:20}")
    private int maxActive;

    @Value("${spring.data.redis.jedis.pool.max-idle:10}")
    private int maxIdle;

    @Value("${spring.data.redis.jedis.pool.min-idle:2}")
    private int minIdle;

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxActive);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        // Block when exhausted but only for maxWaitMs — then throw, not hang.
        config.setBlockWhenExhausted(true);
        config.setMaxWait(Duration.ofMillis(maxWaitMs));

        // Validate connections in the background (idle eviction thread), not on every borrow.
        // testOnBorrow=true was adding a PING round-trip to every getResource() call.
        config.setTestOnBorrow(false);
        config.setTestOnReturn(false);
        config.setTestWhileIdle(true);
        config.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        config.setMinEvictableIdleDuration(Duration.ofSeconds(60));
        config.setNumTestsPerEvictionRun(3);

        if (password != null && !password.isBlank()) {
            return new JedisPool(config, host, port, socketTimeoutMs, password);
        }
        return new JedisPool(config, host, port, socketTimeoutMs);
    }
}
