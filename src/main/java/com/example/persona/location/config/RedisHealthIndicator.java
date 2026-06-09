package com.example.persona.location.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Exposes Redis connectivity status at /actuator/health.
 */
@Slf4j
@Component("locationRedis")
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final JedisPool jedisPool;

    @Override
    public Health health() {
        try (Jedis jedis = jedisPool.getResource()) {
            String pong = jedis.ping();
            long dbSize = jedis.dbSize();
            return Health.up()
                    .withDetail("service", "Redis")
                    .withDetail("ping", pong)
                    .withDetail("keys", dbSize)
                    .build();
        } catch (Exception e) {
            log.debug("Redis health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service", "Redis")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
