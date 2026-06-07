package com.example.persona.session.cache;

import com.example.persona.enums.ErrorCode;
import com.example.persona.exception.AppException;
import com.example.persona.session.model.UserSession;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Cache-aside pattern implementation for UserSession. Uses SHA key for cache
 * lookup. Uses Redis when available, falls back to local ConcurrentHashMap.
 */
@Slf4j
@Component
public class UserSessionCache {

    private static final String CACHE_KEY_PREFIX = "session:sha:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CacheEntry> localCache;
    private final ScheduledExecutorService cleanupExecutor;

    public UserSessionCache(ObjectProvider<StringRedisTemplate> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate.getIfAvailable();
        this.objectMapper = objectMapper;
        this.localCache = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

        this.cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Get session fromEntity cache by SHA key (Cache-Aside read).
     *
     * @param sessionSha
     *            the session SHA key
     * @return Optional containing the session if found in cache
     */
    public Optional<UserSession> getBySha(String sessionSha) {
        if (sessionSha == null) {
            return Optional.empty();
        }
        if (redisTemplate != null) {
            return getFromRedis(sessionSha);
        } else {
            return getFromLocal(sessionSha);
        }
    }

    /**
     * Put session into cache using SHA as key (Cache-Aside write).
     *
     * @param sessionSha
     *            the session SHA key
     * @param session
     *            the session to cache
     */
    public void put(String sessionSha, UserSession session) {
        if (sessionSha == null) {
            log.warn("Cannot cache session with null SHA");
            return;
        }
        if (redisTemplate != null) {
            cacheInRedis(sessionSha, session);
        } else {
            cacheLocally(sessionSha, session);
        }
    }

    /**
     * Evict session fromEntity cache by SHA key.
     *
     * @param sessionSha
     *            the session SHA key
     */
    public void evict(String sessionSha) {
        if (sessionSha == null) {
            return;
        }
        if (redisTemplate != null) {
            removeFromRedis(sessionSha);
        } else {
            removeFromLocal(sessionSha);
        }
    }

    private void cacheInRedis(String sessionSha, UserSession session) {
        if (redisTemplate == null) {
            log.warn("StringRedisTemplate not available, skipping Redis cache for SHA: {}", sessionSha);
            return;
        }
        try {
            String cacheKey = generateCacheKey(sessionSha);
            String jsonValue = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, CACHE_TTL);
            log.debug("Successfully cached UserSession in Redis with SHA: {}", sessionSha);
        } catch (Exception e) {
            log.error("Error caching UserSession in Redis with SHA: {}", sessionSha, e);
            throw new AppException("Failed to cache user session in Redis", ErrorCode.CACHE_ERROR, e);
        }
    }

    private Optional<UserSession> getFromRedis(String sessionSha) {
        if (redisTemplate == null) {
            return Optional.empty();
        }
        try {
            String cacheKey = generateCacheKey(sessionSha);
            String jsonValue = redisTemplate.opsForValue().get(cacheKey);
            if (jsonValue == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(jsonValue, UserSession.class));
        } catch (Exception e) {
            log.error("Error retrieving UserSession from Redis with SHA: {}", sessionSha, e);
            return Optional.empty();
        }
    }

    private void removeFromRedis(String sessionSha) {
        if (redisTemplate == null) return;
        try {
            String cacheKey = generateCacheKey(sessionSha);
            redisTemplate.delete(cacheKey);
            log.debug("Successfully removed UserSession fromEntity Redis with SHA: {}", sessionSha);
        } catch (Exception e) {
            log.error("Error removing UserSession fromEntity Redis with SHA: {}", sessionSha, e);
        }
    }

    private void cacheLocally(String sessionSha, UserSession session) {
        try {
            String cacheKey = generateCacheKey(sessionSha);
            localCache.put(cacheKey, new CacheEntry(session));
            log.debug("Successfully cached UserSession locally with SHA: {}", sessionSha);
        } catch (Exception e) {
            log.error("Error caching UserSession locally with SHA: {}", sessionSha, e);
            throw new AppException("Failed to cache user session locally", ErrorCode.CACHE_ERROR, e);
        }
    }

    private Optional<UserSession> getFromLocal(String sessionSha) {
        try {
            String cacheKey = generateCacheKey(sessionSha);
            CacheEntry entry = localCache.get(cacheKey);
            if (entry == null || entry.isExpired()) {
                if (entry != null) {
                    localCache.remove(cacheKey);
                }
                return Optional.empty();
            }
            return Optional.of(entry.getSession());
        } catch (Exception e) {
            log.error("Error retrieving UserSession fromEntity local cache with SHA: {}", sessionSha, e);
            return Optional.empty();
        }
    }

    private void removeFromLocal(String sessionSha) {
        try {
            String cacheKey = generateCacheKey(sessionSha);
            localCache.remove(cacheKey);
            log.debug("Successfully removed UserSession fromEntity local cache with SHA: {}", sessionSha);
        } catch (Exception e) {
            log.error("Error removing UserSession fromEntity local cache with SHA: {}", sessionSha, e);
        }
    }

    private void cleanupExpiredEntries() {
        localCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private String generateCacheKey(String sessionSha) {
        return CACHE_KEY_PREFIX + sessionSha;
    }

    private static class CacheEntry {
        private final UserSession session;
        private final long expirationTime;

        public CacheEntry(UserSession session) {
            this.session = session;
            this.expirationTime = System.currentTimeMillis() + CACHE_TTL.toMillis();
        }

        public UserSession getSession() {
            return session;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
