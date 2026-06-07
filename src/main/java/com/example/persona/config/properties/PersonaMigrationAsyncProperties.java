package com.example.persona.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Pool sizing for {@code migrationTaskExecutor} only ({@code @Async("migrationTaskExecutor")}).
 * Bind from {@code persona.migration.async.*}; fields keep defaults when properties are absent.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "persona.migration.async")
public class PersonaMigrationAsyncProperties {

    private int corePoolSize = 10;

    private int maxPoolSize = 20;

    /**
     * Bounded queue. When full and all threads are busy, the default {@link java.util.concurrent.RejectedExecutionHandler}
     * is {@link java.util.concurrent.ThreadPoolExecutor.AbortPolicy}: submit fails with
     * {@link java.util.concurrent.RejectedExecutionException} (no JVM shutdown / no Spring restart).
     */
    private int queueCapacity = 500;
}
