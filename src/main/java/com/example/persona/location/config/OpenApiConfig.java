package com.example.persona.location.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI locationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wing Bank Location Service API")
                        .description("""
                                Unified location data service for Wing Bank App and Website.

                                Provides Branch, ATM/CRM, Agent and Master Agent locations
                                across Cambodia. Supports full-text search, geographic proximity
                                search, and type-based filtering.

                                **Search** powered by Meilisearch (PostgreSQL fallback).
                                **Proximity** powered by PostGIS ST_DWithin.
                                **Caching** via Redis (Jedis).
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Wing Bank Engineering").email("engineering@wingbank.com.kh"))
                        .license(new License().name("Internal — Wing Bank").url("https://wingbank.com.kh")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local"),
                        new Server()
                                .url("https://api-dev.wingbank.com.kh/location")
                                .description("Dev"),
                        new Server().url("https://api.wingbank.com.kh/location").description("Production")))
                // Placeholder for JWT bearer — uncomment when wing-security is plugged in
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Wing Bank JWT token (wing-token)")));
    }
}
