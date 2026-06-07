package com.example.persona.cache;

import com.example.persona.config.properties.SystemProperties;
import com.example.persona.enums.ErrorCode;
import com.example.persona.exception.AppException;
import com.example.persona.model.CustomerPersona;
import com.example.persona.theme.dto.response.UserPersonaResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class UserPersonaCache {
    private final SystemProperties systemProperties;
    private static final String CACHE_KEY_PREFIX = "CUSTOMER:PERSONA:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CacheEntry> localCache;
    private final ScheduledExecutorService cleanupExecutor;

    public UserPersonaCache(
            ObjectProvider<StringRedisTemplate> redisTemplate,
            SystemProperties systemProperties,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate.getIfAvailable();
        this.objectMapper = objectMapper;
        this.localCache = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        this.systemProperties = systemProperties;
        // Schedule cleanup of expired entries
        this.cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.HOURS);
    }

    public void cacheUserPersona(String customerKey, UserPersonaResponse persona) {
        CustomerPersona customerPersona = this.getCustomerPersona(customerKey).orElse(new CustomerPersona());
        customerPersona.setCustomerKey(customerKey);
        customerPersona.setPersona(persona);
        if (redisTemplate != null) {
            cacheInRedis(customerKey, customerPersona);
        } else {
            cacheLocally(customerKey, customerPersona);
        }
    }

    public Optional<CustomerPersona> cacheCustomerPersona(String customerId, CustomerPersona persona) {
        if (redisTemplate != null) {
            cacheInRedis(customerId, persona);
        } else {
            cacheLocally(customerId, persona);
        }
        return this.getCustomerPersona(customerId);
    }

    public Optional<CustomerPersona> getCustomerPersona(String customerKey) {
        if (redisTemplate != null) {
            return getFromRedis(customerKey);
        } else {
            return getFromLocal(customerKey);
        }
    }

    public void removeCustomerPersona(String customerKey) {
        if (redisTemplate != null) {
            removeFromRedis(customerKey);
        } else {
            removeFromLocal(customerKey);
        }
    }

    private void cacheInRedis(String customerKey, CustomerPersona persona) {
        if (redisTemplate == null) return;
        try {
            String cacheKey = generateCacheKey(customerKey);
            String jsonValue = objectMapper.writeValueAsString(persona);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, CACHE_TTL.toSeconds());
            log.debug("Successfully cached CustomerPersona in Redis for customerKey: {}", customerKey);
        } catch (Exception e) {
            log.error("Error caching CustomerPersona in Redis for customerKey: {}", customerKey, e);
            throw new AppException("Failed to cache customer persona in Redis", ErrorCode.CACHE_ERROR, e);
        }
    }

    private Optional<CustomerPersona> getFromRedis(String customerKey) {
        if (redisTemplate == null) return Optional.empty();
        try {
            String cacheKey = generateCacheKey(customerKey);
            String jsonValue = redisTemplate.opsForValue().get(cacheKey);
            if (jsonValue == null) {
                return Optional.empty();
            }
            CustomerPersona persona = objectMapper.readValue(jsonValue, CustomerPersona.class);
            return Optional.of(persona);
        } catch (Exception e) {
            log.error("Error retrieving CustomerPersona fromEntity Redis for customerKey: {}", customerKey, e);
            throw new AppException("Failed to retrieve customer persona fromEntity Redis", ErrorCode.CACHE_ERROR, e);
        }
    }

    private void removeFromRedis(String customerKey) {
        if (redisTemplate == null) return;
        try {
            String cacheKey = generateCacheKey(customerKey);
            redisTemplate.delete(cacheKey);
            log.debug("Successfully removed CustomerPersona fromEntity Redis for customerKey: {}", customerKey);
        } catch (Exception e) {
            log.error("Error removing CustomerPersona fromEntity Redis for customerKey: {}", customerKey, e);
            throw new AppException("Failed to remove customer persona fromEntity Redis", ErrorCode.CACHE_ERROR, e);
        }
    }

    private void cacheLocally(String customerKey, CustomerPersona persona) {
        try {
            String cacheKey = generateCacheKey(customerKey);
            localCache.put(cacheKey, new CacheEntry(persona));
            log.debug("Successfully cached CustomerPersona locally for customerKey: {}", customerKey);
        } catch (Exception e) {
            log.error("Error caching CustomerPersona locally for customerKey: {}", customerKey, e);
            throw new AppException("Failed to cache customer persona locally", ErrorCode.CACHE_ERROR, e);
        }
    }

    private Optional<CustomerPersona> getFromLocal(String customerKey) {
        try {
            String cacheKey = generateCacheKey(customerKey);
            CacheEntry entry = localCache.get(cacheKey);
            if (entry == null || entry.isExpired()) {
                if (entry != null) {
                    localCache.remove(cacheKey);
                }
                return Optional.empty();
            }
            return Optional.of(entry.getPersona());
        } catch (Exception e) {
            log.error("Error retrieving CustomerPersona fromEntity local cache for customerKey: {}", customerKey, e);
            throw new AppException(
                    "Failed to retrieve customer persona fromEntity local cache", ErrorCode.CACHE_ERROR, e);
        }
    }

    private void removeFromLocal(String customerKey) {
        try {
            String cacheKey = generateCacheKey(customerKey);
            localCache.remove(cacheKey);
            log.debug("Successfully removed CustomerPersona fromEntity local cache for customerKey: {}", customerKey);
        } catch (Exception e) {
            log.error("Error removing CustomerPersona fromEntity local cache for customerKey: {}", customerKey, e);
            throw new AppException(
                    "Failed to remove customer persona fromEntity local cache", ErrorCode.CACHE_ERROR, e);
        }
    }

    private void cleanupExpiredEntries() {
        localCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private String generateCacheKey(String customerKey) {
        return systemProperties.getEnv() + ":" + CACHE_KEY_PREFIX + customerKey;
    }

    private static class CacheEntry {
        @Getter
        private final CustomerPersona persona;

        private final long expirationTime;

        public CacheEntry(CustomerPersona persona) {
            this.persona = persona;
            this.expirationTime = System.currentTimeMillis() + CACHE_TTL.toMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
