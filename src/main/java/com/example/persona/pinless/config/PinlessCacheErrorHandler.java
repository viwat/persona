package com.example.persona.pinless.config;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

@Slf4j
public class PinlessCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException ex, Cache cache, @NonNull Object key) {
        log.warn(
                "[PINLESS] Cache GET failed — falling back to DB: cache={} key={} error={}",
                cache.getName(),
                key,
                ex.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException ex, Cache cache, @NonNull Object key, Object value) {
        log.warn(
                "[PINLESS] Cache PUT failed — result not cached: cache={} key={} error={}",
                cache.getName(),
                key,
                ex.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException ex, Cache cache, @NonNull Object key) {
        log.warn(
                "[PINLESS] Cache EVICT failed — stale entry may remain until TTL: cache={} key={} error={}",
                cache.getName(),
                key,
                ex.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException ex, Cache cache) {
        log.warn("[PINLESS] Cache CLEAR failed: cache={} error={}", cache.getName(), ex.getMessage());
    }
}
