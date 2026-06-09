package com.example.persona.location.service;

import com.example.persona.location.model.Location;
import com.example.persona.location.model.LocationType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationCacheService {

    private static final String KEY_PREFIX_LOCATION = "location:id:";
    private static final String KEY_PREFIX_ALL = "location:all:";
    private static final String KEY_ALL_WILDCARD = "location:*";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${location.cache.ttl-seconds:300}")
    private int ttlSeconds;

    @Value("${location.cache.all-locations-ttl-seconds:600}")
    private int allLocationsTtlSeconds;

    @CircuitBreaker(name = "redis", fallbackMethod = "getByIdFallback")
    public Optional<Location> getById(UUID id) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX_LOCATION + id);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, Location.class));
        } catch (Exception e) {
            log.warn("Redis getById failed for {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "putByIdFallback")
    public void putById(Location location) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            KEY_PREFIX_LOCATION + location.getId(),
                            objectMapper.writeValueAsString(location),
                            Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("Redis putById failed for {}: {}", location.getId(), e.getMessage());
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "evictByIdFallback")
    public void evictById(UUID id) {
        try {
            redisTemplate.delete(KEY_PREFIX_LOCATION + id);
        } catch (Exception e) {
            log.warn("Redis evictById failed for {}: {}", id, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "getAllByTypesFallback")
    public Optional<List<Location>> getAllByTypes(List<LocationType> types) {
        try {
            String json = redisTemplate.opsForValue().get(buildAllKey(types));
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, new TypeReference<>() {}));
        } catch (Exception e) {
            log.warn("Redis getAllByTypes failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "putAllByTypesFallback")
    public void putAllByTypes(List<LocationType> types, List<Location> locations) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            buildAllKey(types),
                            objectMapper.writeValueAsString(locations),
                            Duration.ofSeconds(allLocationsTtlSeconds));
        } catch (Exception e) {
            log.warn("Redis putAllByTypes failed: {}", e.getMessage());
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "evictAllFallback")
    public void evictAll() {
        try {
            List<String> keys = new ArrayList<>();
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                try (Cursor<byte[]> cursor = connection
                        .keyCommands()
                        .scan(ScanOptions.scanOptions()
                                .match(KEY_ALL_WILDCARD)
                                .count(100)
                                .build())) {
                    cursor.forEachRemaining(k -> keys.add(new String(k, StandardCharsets.UTF_8)));
                }
                return null;
            });
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis evictAll failed: {}", e.getMessage());
        }
    }

    // ── Fallbacks ─────────────────────────────────────────────────────────────

    public Optional<Location> getByIdFallback(UUID id, Throwable t) {
        log.warn("Redis circuit open for getById({}): {}", id, t.getMessage());
        return Optional.empty();
    }

    public Optional<List<Location>> getAllByTypesFallback(List<LocationType> types, Throwable t) {
        log.warn("Redis circuit open for getAllByTypes: {}", t.getMessage());
        return Optional.empty();
    }

    public void putByIdFallback(Location location, Throwable t) {
        log.warn("Redis circuit open — skipping putById({}): {}", location.getId(), t.getMessage());
    }

    public void evictByIdFallback(UUID id, Throwable t) {
        log.warn("Redis circuit open — skipping evictById({}): {}", id, t.getMessage());
    }

    public void putAllByTypesFallback(List<LocationType> types, List<Location> locations, Throwable t) {
        log.warn("Redis circuit open — skipping putAllByTypes: {}", t.getMessage());
    }

    public void evictAllFallback(Throwable t) {
        log.warn("Redis circuit open — skipping evictAll: {}", t.getMessage());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String buildAllKey(List<LocationType> types) {
        if (types == null || types.isEmpty()) return KEY_PREFIX_ALL + "ALL";
        String typeKey = types.stream().map(LocationType::getCode).sorted().reduce("", (a, b) -> a + "_" + b);
        return KEY_PREFIX_ALL + typeKey;
    }
}
