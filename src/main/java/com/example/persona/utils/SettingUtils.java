package com.example.persona.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettingUtils {

    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final RedisKeyUtils redisKeyUtils;

    public SettingUtils(ObjectProvider<StringRedisTemplate> redisTemplate, RedisKeyUtils redisKeyUtils) {
        this.redisTemplate = redisTemplate;
        this.redisKeyUtils = redisKeyUtils;
    }

    public void updateGlobalTimestamp(String key, Long timestamp) {
        StringRedisTemplate template = redisTemplate.getIfAvailable();
        if (template == null) {
            log.warn("StringRedisTemplate not available, skipping global timestamp update for key: {}", key);
            return;
        }

        try {
            String redisKey = redisKeyUtils.getPublicSettingKey();
            HashOperations<String, String, String> hashOps = template.opsForHash();

            hashOps.put(redisKey, key, String.valueOf(timestamp));
            template.expire(redisKey, Duration.ofSeconds(86400));

            log.info("Updated global timestamp for key: {}, timestamp: {}", key, timestamp);
        } catch (Exception e) {
            log.error("Failed to update global timestamp for key: {}", key, e);
            log.warn("Continuing without updating global timestamp");
        }
    }

    public void updateGlobalSetting(String key) {
        this.updateGlobalTimestamp(
                key, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
    }
}
