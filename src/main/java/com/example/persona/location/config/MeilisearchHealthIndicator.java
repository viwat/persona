package com.example.persona.location.config;

import com.meilisearch.sdk.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Exposes Meilisearch connectivity status at /actuator/health.
 */
@Slf4j
@Component("meilisearch")
@RequiredArgsConstructor
public class MeilisearchHealthIndicator implements HealthIndicator {

    private final Client meilisearchClient;

    @Override
    public Health health() {
        try {
            boolean healthy = meilisearchClient.isHealthy();
            if (healthy) {
                return Health.up()
                        .withDetail("service", "Meilisearch")
                        .withDetail("status", "connected")
                        .build();
            } else {
                return Health.down()
                        .withDetail("service", "Meilisearch")
                        .withDetail("status", "unhealthy")
                        .build();
            }
        } catch (Exception e) {
            log.debug("Meilisearch health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service", "Meilisearch")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
