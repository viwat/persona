package com.example.persona.i18n.service;

import com.example.persona.enums.ErrorCode;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.AppException;
import com.example.persona.i18n.dto.request.TranslationCreateRequest;
import com.example.persona.i18n.dto.request.TranslationModifyRequest;
import com.example.persona.i18n.dto.response.TranslationResponse;
import com.example.persona.i18n.mapper.TranslationMapper;
import com.example.persona.i18n.model.Translation;
import com.example.persona.i18n.repository.TranslationRepository;
import com.example.persona.utils.LanguageUtils;
import com.example.persona.utils.RedisKeyUtils;
import com.example.persona.utils.SettingUtils;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {
    private final TranslationMapper translationMapper;
    private final TranslationRepository translationRepository;
    private final RedisKeyUtils redisKeyUtils;
    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SettingUtils settingUtils;

    private static final String TRANSLATION_KEY = "translation";

    @Transactional
    public TranslationResponse createTranslation(TranslationCreateRequest request) {
        log.debug("Creating new translation with request: {}", request);
        try {
            // Check if translation already exists
            Translation existing = translationRepository.findByCodeAndAppCode(request.getCode(), request.getAppCode());
            if (existing != null) {
                log.error(
                        "Translation already exists for code: {} and appCode: {}",
                        request.getCode(),
                        request.getAppCode());
                throw new AppException(
                        String.format(
                                "Translation already exists for code: %s and appCode: %s",
                                request.getCode(), request.getAppCode()),
                        ErrorCode.TRANSLATION_ALREADY_EXISTS);
            }
            // Map request to entity
            Translation translation = translationMapper.toEntity(request);
            translation.setStatus(StatusType.ACTIVE);
            // Save entity
            translation = translationRepository.save(translation);
            // Update global settings
            settingUtils.updateGlobalSetting(TRANSLATION_KEY);
            // Map entity to response with language
            TranslationResponse response = translationMapper.toResponse(translation, LanguageUtils.getLanguage());
            log.debug("Translation created successfully: {}", response);

            // Evict Redis cache for this app's translations
            String cacheKey = redisKeyUtils.getTranslationsKey(request.getAppCode());
            redisTemplate.ifAvailable(t -> {
                try {
                    t.delete(cacheKey);
                    log.debug("Evicted Redis cache for key: {}", cacheKey);
                } catch (Exception e) {
                    log.warn("Failed to evict Redis cache for key: {}. Error: {}", cacheKey, e.getMessage());
                }
            });

            return response;
        } catch (AppException e) {
            log.error("Failed to create translation. Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating translation. Error: {}", e.getMessage(), e);
            throw new AppException("Failed to create translation", ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public TranslationResponse modifyTranslation(TranslationModifyRequest request) {
        log.debug("Updating translation with request: {}", request);
        try {
            Translation translation =
                    translationRepository.findByCodeAndAppCode(request.getCode(), request.getAppCode());
            if (translation == null) {
                log.error(
                        "Translation not found for code: {} and appCode: {}", request.getCode(), request.getAppCode());
                throw new AppException("Translation not found", ErrorCode.TRANSLATION_NOT_FOUND);
            }
            translationMapper.updateEntityFromRequest(request, translation);
            translationRepository.save(translation);

            settingUtils.updateGlobalSetting(TRANSLATION_KEY);
            TranslationResponse response = translationMapper.toResponse(translation, LanguageUtils.getLanguage());
            log.debug("Translation updated successfully: {}", response);

            // Evict Redis cache for this app's translations
            String cacheKey = redisKeyUtils.getTranslationsKey(request.getAppCode());
            redisTemplate.ifAvailable(t -> {
                try {
                    t.delete(cacheKey);
                    log.debug("Evicted Redis cache for key: {}", cacheKey);
                } catch (Exception e) {
                    log.warn("Failed to evict Redis cache for key: {}. Error: {}", cacheKey, e.getMessage());
                }
            });

            return response;
        } catch (AppException e) {
            log.error("Failed to update translation. Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while updating translation. Error: {}", e.getMessage(), e);
            throw new AppException("Failed to update translation", ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<TranslationResponse> getTranslations(String appCode) {
        log.debug("Getting all active translations for appCode: {}", appCode);
        String language = LanguageUtils.getLanguage();
        String cacheKey = redisKeyUtils.getTranslationsKey(appCode);

        StringRedisTemplate template = redisTemplate.getIfAvailable();

        // If Redis is not available, fetch directly from database
        if (template == null) {
            log.debug("Redis not available, fetching translations from database");
            return fetchTranslationsFromDatabase(appCode, language);
        }
        try {
            String cachedData = template.opsForValue().get(cacheKey);
            if (cachedData != null) {
                log.debug("Found cached translations for appCode: {}", appCode);
                try {
                    List<TranslationResponse> cachedTranslations =
                            objectMapper.readValue(cachedData, new TypeReference<>() {});
                    log.debug("Successfully retrieved {} translations from cache", cachedTranslations.size());
                    return cachedTranslations;
                } catch (JacksonException e) {
                    log.error("Error deserializing cached translations. Error: {}", e.getMessage(), e);
                    // Fallback to database on cache deserialization error
                    return fetchTranslationsFromDatabase(appCode, language);
                }
            }
            // Cache miss - fetch from database and cache the result
            List<TranslationResponse> translations = fetchTranslationsFromDatabase(appCode, language);
            cacheTranslations(cacheKey, translations);
            return translations;
        } catch (Exception e) {
            log.error("Redis operation failed for appCode: {}. Error: {}", appCode, e.getMessage(), e);
            // Fallback to database on Redis error
            return fetchTranslationsFromDatabase(appCode, language);
        }
    }

    private List<TranslationResponse> fetchTranslationsFromDatabase(String appCode, String language) {
        log.debug("Fetching translations from database for appCode: {}", appCode);
        return translationRepository.findAllActiveByAppCode(appCode).stream()
                .map(t -> translationMapper.toResponse(t, language))
                .toList();
    }

    private void cacheTranslations(String cacheKey, List<TranslationResponse> translations) {
        StringRedisTemplate template = redisTemplate.getIfAvailable();
        if (template == null) {
            return;
        }

        try {
            String jsonData = objectMapper.writeValueAsString(translations);
            template.opsForValue().set(cacheKey, jsonData, Duration.ofSeconds(3600));
            log.debug("Successfully cached {} translations", translations.size());
        } catch (Exception e) {
            log.error("Error serializing translations for cache. Error: {}", e.getMessage(), e);
            // Continue without caching - the translations will still be returned
        }
    }

    @Transactional
    public void deleteTranslation(Long id) {
        log.debug("Deleting translation with id: {}", id);
        try {
            translationRepository.deleteById(id);
            settingUtils.updateGlobalSetting(TRANSLATION_KEY);
            log.debug("Translation deleted successfully");

            String cacheKey = redisKeyUtils.getTranslationsKey(String.valueOf(id));
            redisTemplate.ifAvailable(t -> {
                try {
                    t.delete(cacheKey);
                    log.debug("Evicted Redis cache for key: {}", cacheKey);
                } catch (Exception e) {
                    log.warn("Failed to evict Redis cache for key: {}. Error: {}", cacheKey, e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Failed to delete translation with id: {}. Error: {}", id, e.getMessage(), e);
            throw new AppException("Failed to delete translation", ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public String getTranslation(String code) {
        log.debug("Getting translation for code: {}", code);
        Translation translation = translationRepository
                .findByCode(code)
                .orElseThrow(() -> new AppException("Translation not found", ErrorCode.TRANSLATION_NOT_FOUND));
        String language = LanguageUtils.getLanguage();
        return translationMapper.getTranslationByLanguage(translation, language);
    }

    @Transactional(readOnly = true)
    public String getTranslationOrDefault(String code, String defaultValue) {
        if (code == null) {
            return defaultValue;
        }
        return translationRepository
                .findByCode(code)
                .map(translation ->
                        translationMapper.getTranslationByLanguage(translation, LanguageUtils.getLanguage()))
                .orElse(defaultValue);
    }
}
