package com.example.persona.location.service;

import com.example.persona.location.model.Location;
import com.example.persona.location.model.LocationType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationCacheService {

    private static final String KEY_PREFIX_LOCATION = "location:id:";
    private static final String KEY_PREFIX_ALL = "location:all:";
    private static final String KEY_ALL_WILDCARD = "location:*";

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    @Value("${location.cache.ttl-seconds:300}")
    private int ttlSeconds;

    @Value("${location.cache.all-locations-ttl-seconds:600}")
    private int allLocationsTtlSeconds;

    @CircuitBreaker(name = "redis", fallbackMethod = "getByIdFallback")
    public Optional<Location> getById(UUID id) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(KEY_PREFIX_LOCATION + id);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, Location.class));
        } catch (Exception e) {
            log.warn("Redis getById failed for {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "putByIdFallback")
    public void putById(Location location) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(KEY_PREFIX_LOCATION + location.getId(), ttlSeconds, objectMapper.writeValueAsString(location));
        } catch (Exception e) {
            log.warn("Redis putById failed for {}: {}", location.getId(), e.getMessage());
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "evictByIdFallback")
    public void evictById(UUID id) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(KEY_PREFIX_LOCATION + id);
        } catch (Exception e) {
            log.warn("Redis evictById failed for {}: {}", id, e.getMessage());
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "getAllByTypesFallback")
    public Optional<List<Location>> getAllByTypes(List<LocationType> types) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(buildAllKey(types));
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, new TypeReference<>() {}));
        } catch (Exception e) {
            log.warn("Redis getAllByTypes failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "putAllByTypesFallback")
    public void putAllByTypes(List<LocationType> types, List<Location> locations) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(buildAllKey(types), allLocationsTtlSeconds, objectMapper.writeValueAsString(locations));
        } catch (Exception e) {
            log.warn("Redis putAllByTypes failed: {}", e.getMessage());
        }
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "evictAllFallback")
    public void evictAll() {
        try (Jedis jedis = jedisPool.getResource()) {
            String cursor = "0";
            do {
                ScanParams params = new ScanParams().match(KEY_ALL_WILDCARD).count(100);
                ScanResult<String> result = jedis.scan(cursor, params);
                cursor = result.getCursor();
                List<String> keys = result.getResult();
                if (!keys.isEmpty()) jedis.del(keys.toArray(new String[0]));
            } while (!"0".equals(cursor));
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
