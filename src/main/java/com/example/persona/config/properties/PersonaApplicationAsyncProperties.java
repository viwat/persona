package com.example.persona.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Pool sizing for the default {@code @Async} executor (bean {@code taskExecutor}): search, location,
 * transactions, etc. Not used for migration.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "persona.application.async")
public class PersonaApplicationAsyncProperties {

    private int corePoolSize = 4;

    private int maxPoolSize = 16;

    private int queueCapacity = 200;
}
