package com.example.persona.service;

import com.example.persona.dto.response.HomeScreenMetadata;
import com.example.persona.enums.ErrorCode;
import com.example.persona.exception.AppException;
import com.example.persona.utils.RedisKeyUtils;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateService {

    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final RedisKeyUtils redisKeyUtils;

    @Transactional(readOnly = true)
    public HomeScreenMetadata checkUpdates(LocalDateTime lastSyncTimestamp, String customerKey, String event) {
        log.info("Checking updates for customer: {}, event: {}", customerKey, event);
        if (!List.of("home", "payment").contains(event)) {
            throw new AppException("Invalid event", ErrorCode.EVENT_UPDATE_NOT_FOUND);
        }

        StringRedisTemplate template = redisTemplate.getIfAvailable();

        // If Redis is not available, return default metadata
        if (template == null) {
            log.debug("Redis not available, returning default metadata");
            return createDefaultMetadata(lastSyncTimestamp);
        }

        try {
            // Get user's latest timestamp
            Map<String, Long> userLatestTimestamp = getUserLatestTimestamp(template, customerKey);

            // Get global settings
            Map<String, Long> globalSettings = getGlobalSettings(template);

            // Log the event check
            log.debug("Checking updates for customer: {}, event: {}", customerKey, event);
            Map<String, Long> eventLatestTimestamp =
                    getUserLatestTimestampByCustomerAndEvent(template, customerKey, event);

            // Combine both maps for checking
            Map<String, Long> allTimestamps = new HashMap<>();
            if (userLatestTimestamp != null) {
                allTimestamps.putAll(userLatestTimestamp);
            }
            allTimestamps.putAll(globalSettings);
            allTimestamps.putAll(eventLatestTimestamp);

            // Find keys with timestamps newer than lastSyncTimestamp
            long lastSyncEpochMilli =
                    lastSyncTimestamp.toInstant(ZoneOffset.UTC).toEpochMilli();
            List<String> updatedKeys = allTimestamps.entrySet().stream()
                    .filter(entry -> entry.getValue() > lastSyncEpochMilli)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // Get the most recent timestamp
            long latestTimestamp = allTimestamps.values().stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(lastSyncEpochMilli);

            return HomeScreenMetadata.builder()
                    .lastUpdateTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(latestTimestamp), ZoneOffset.UTC))
                    .hasUpdates(!updatedKeys.isEmpty())
                    .updatedKeys(updatedKeys)
                    .build();

        } catch (Exception e) {
            log.error("Error checking updates", e);
            // Return default metadata on error
            return createDefaultMetadata(lastSyncTimestamp);
        }
    }

    private HomeScreenMetadata createDefaultMetadata(LocalDateTime lastSyncTimestamp) {
        return HomeScreenMetadata.builder()
                .lastUpdateTimestamp(lastSyncTimestamp)
                .hasUpdates(false)
                .updatedKeys(List.of())
                .build();
    }

    private Map<String, Long> getUserLatestTimestamp(StringRedisTemplate template, String userId) {
        String userKey = redisKeyUtils.getUserSettingKey(userId);
        HashOperations<String, String, String> hashOps = template.opsForHash();

        Map<String, String> settingsHash = hashOps.entries(userKey);
        if (settingsHash.isEmpty()) {
            return Collections.emptyMap();
        }

        return settingsHash.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Long.parseLong(entry.getValue())));
    }

    private Map<String, Long> getGlobalSettings(StringRedisTemplate template) {
        String globalKey = redisKeyUtils.getPublicSettingKey();
        HashOperations<String, String, String> hashOps = template.opsForHash();

        Map<String, String> settingsHash = hashOps.entries(globalKey);
        if (!settingsHash.isEmpty()) {
            return settingsHash.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> Long.parseLong(entry.getValue())));
        }

        Map<String, String> defaults = new HashMap<>();
        defaults.put("lastUpdateTimestamp", String.valueOf(System.currentTimeMillis()));
        defaults.put("forceUpdate", "0");

        hashOps.putAll(globalKey, defaults);
        template.expire(globalKey, Duration.ofSeconds(100));

        return defaults.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Long.parseLong(entry.getValue())));
    }

    private LocalDateTime getLatestUpdateTimestamp() {
        return LocalDateTime.now();
    }

    private Map<String, Long> getUserLatestTimestampByCustomerAndEvent(
            StringRedisTemplate template, String customerId, String event) {
        String userEventKey = redisKeyUtils.getUserSettingKey(customerId) + ":" + event;
        HashOperations<String, String, String> hashOps = template.opsForHash();

        Map<String, String> settingsHash = hashOps.entries(userEventKey);
        if (!settingsHash.isEmpty()) {
            return settingsHash.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> Long.parseLong(entry.getValue())));
        }

        Map<String, String> initialHash = new HashMap<>();
        initialHash.put("lastUpdateTimestamp", String.valueOf(System.currentTimeMillis()));

        hashOps.putAll(userEventKey, initialHash);
        template.expire(userEventKey, Duration.ofSeconds(3600));

        return initialHash.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Long.parseLong(entry.getValue())));
    }
}
