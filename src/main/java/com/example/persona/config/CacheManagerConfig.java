package com.example.persona.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class CacheManagerConfig {

    public static final String CACHE_ACTIVE_ANNOUNCEMENTS = "active_announcements";
    public static final String CACHE_MIGRATION_STAGES = "migrationStages";

    @Primary
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CACHE_ACTIVE_ANNOUNCEMENTS, CACHE_MIGRATION_STAGES);
    }
}
