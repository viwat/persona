package com.example.persona.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class VaultProperties {

    @Value("${redis.datasource.password:}")
    private String redisPassword;
}
