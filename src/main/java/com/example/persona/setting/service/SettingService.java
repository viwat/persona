package com.example.persona.setting.service;

import com.example.persona.enums.SettingCategory;
import com.example.persona.enums.SettingPermission;
import com.example.persona.enums.SettingType;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.setting.dto.request.SettingCreateRequest;
import com.example.persona.setting.dto.request.SettingModifyRequest;
import com.example.persona.setting.dto.request.UserSettingCreateRequest;
import com.example.persona.setting.dto.response.SettingResponse;
import com.example.persona.setting.dto.response.UserSettingHistoryResponse;
import com.example.persona.setting.dto.response.UserSettingResponse;
import com.example.persona.setting.mapper.SettingMapper;
import com.example.persona.setting.mapper.UserSettingHistoryMapper;
import com.example.persona.setting.mapper.UserSettingMapper;
import com.example.persona.setting.model.Setting;
import com.example.persona.setting.model.UserSetting;
import com.example.persona.setting.model.UserSettingHistory;
import com.example.persona.setting.repository.SettingRepository;
import com.example.persona.setting.repository.UserSettingHistoryRepository;
import com.example.persona.setting.repository.UserSettingRepository;
import com.example.persona.utils.EnumUtils;
import com.example.persona.utils.ObjectMapperUtils;
import com.example.persona.utils.RedisKeyUtils;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingService {
    private final SettingRepository settingRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserSettingHistoryRepository userSettingHistoryRepository;

    private final SettingMapper settingMapper;
    private final UserSettingMapper userSettingMapper;
    private final UserSettingHistoryMapper userSettingHistoryMapper;

    private final ObjectMapperUtils objectMapperUtils;
    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final RedisKeyUtils redisKeyUtils;

    @Transactional
    public SettingResponse createSetting(SettingCreateRequest request) {
        if (settingRepository.existsBySettingKey(request.getSettingKey())) {
            throw new BusinessException("Setting key already exists: " + request.getSettingKey());
        }
        Setting setting = buildSetting(request);
        settingRepository.save(setting);
        return settingMapper.toResponse(setting);
    }

    @Transactional
    public List<SettingResponse> createSettings(List<SettingCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException("Setting list must not be empty");
        }
        Set<String> duplicateCheck = new HashSet<>();
        for (SettingCreateRequest req : requests) {
            if (!duplicateCheck.add(req.getSettingKey())) {
                throw new BusinessException("Duplicate settingKey in request: " + req.getSettingKey());
            }
        }
        List<String> keys =
                requests.stream().map(SettingCreateRequest::getSettingKey).collect(Collectors.toList());
        List<String> existingKeys = settingRepository.findExistingSettingKeys(keys);
        if (!existingKeys.isEmpty()) {
            throw new BusinessException("Setting keys already exist: " + existingKeys);
        }
        List<Setting> settings = requests.stream().map(this::buildSetting).collect(Collectors.toList());
        settingRepository.saveAll(settings);
        return settings.stream().map(settingMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public UserSettingResponse createUserSetting(UserSettingCreateRequest request) {
        log.info("Creating user setting '{}' for customerKey '{}'", request.getSettingKey(), request.getCustomerKey());
        Setting setting = settingRepository
                .findBySettingKeyAndActive(request.getSettingKey())
                .orElseThrow(() -> new BusinessException("Setting not found or disabled: " + request.getSettingKey()));
        UserSetting userSetting = userSettingMapper.toEntity(request);
        userSetting.setSetting(setting);
        userSetting.setStatus(StatusType.ACTIVE);
        userSettingRepository.save(userSetting);
        return userSettingMapper.toResponse(userSetting);
    }

    @Transactional
    public UserSettingResponse modifyUserSetting(Long userSettingId, SettingModifyRequest request) {
        log.info("Updating setting '{}' for customer '{}'", request.getSettingKey(), request.getCustomerNo());
        // Core update logic extracted to helper
        Setting setting = settingRepository
                .findBySettingKeyAndActive(request.getSettingKey())
                .orElseThrow(() ->
                        new BusinessException("Setting not found or disabled: setting_key " + request.getSettingKey()));

        UserSetting userSetting = userSettingRepository
                .findByIdAndCustomerKeyAndStatus(userSettingId, request.getCustomerKey(), StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "UserSetting not found or disabled for customerKey: " + request.getCustomerKey()));

        UserSettingHistory history = getUserSettingsHistory(request, userSetting, setting);

        userSetting.setSettingValue(request.getSettingValue());

        userSettingRepository.save(userSetting);
        userSettingHistoryRepository.save(history);

        return userSettingMapper.toResponse(userSetting);
    }

    @Transactional
    public SettingResponse deleteSetting(String settingKey) {
        Setting setting = settingRepository
                .findBySettingKey(settingKey)
                .orElseThrow(() -> new BusinessException("Setting not found"));
        setting.setStatus(StatusType.INACTIVE);
        settingRepository.save(setting);
        return settingMapper.toResponse(setting);
    }

    @Transactional(readOnly = true)
    public UserSettingResponse getUserSettingValue(Long userSettingId, String customerKey) {
        log.info("Fetching active settings for customerKey: {}", customerKey);
        String cacheKey = redisKeyUtils.getUserSettingKey(userSettingId, customerKey);

        StringRedisTemplate template = redisTemplate.getIfAvailable();

        if (template != null) {
            try {
                String cached = template.opsForValue().get(cacheKey);
                if (cached != null) {
                    return objectMapperUtils.readValue(cached, new TypeReference<>() {});
                }
            } catch (Exception e) {
                log.warn("Redis read failed for key: {}, proceeding to database. Error: {}", cacheKey, e.getMessage());
            }
        }
        UserSetting setting = userSettingRepository
                .findByIdAndCustomerKeyAndStatus(userSettingId, customerKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException("UserSetting not found or disabled: " + customerKey));
        UserSettingResponse response = userSettingMapper.toResponse(setting);
        if (template != null) {
            try {
                template.opsForValue()
                        .set(cacheKey, objectMapperUtils.writeValueAsString(response), Duration.ofSeconds(600));
            } catch (Exception e) {
                log.warn(
                        "Redis write failed for key: {}, cache miss will occur next call. Error: {}",
                        cacheKey,
                        e.getMessage());
            }
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<UserSettingResponse> getUserSettingValues(String customerKey) {
        log.info("Fetching active settings for customerNo: {}", customerKey);
        List<UserSetting> userSettings = userSettingRepository.findByCustomerKeyAndActive(customerKey);
        return userSettings.stream().map(userSettingMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public String getSettingDefaultValue(String settingKey) {
        Setting setting = settingRepository
                .findBySettingKeyAndActive(settingKey)
                .orElseThrow(() -> new BusinessException("Setting not found or disabled: " + settingKey));
        return setting.getDefaultValue();
    }

    @Transactional(readOnly = true)
    public String getSettingValue(String customerKey, String settingKey, String channelCode) {
        Optional<String> userValue = userSettingRepository
                .findByCustomerKeyAndSettingKeyAndChannelCodeAndStatus(
                        customerKey, settingKey, channelCode, StatusType.ACTIVE)
                .map(UserSetting::getSettingValue)
                .filter(StringUtils::hasText);
        return userValue.orElseGet(() -> settingRepository
                .findBySettingKey(settingKey)
                .map(Setting::getDefaultValue)
                .orElse(null));
    }

    private Setting buildSetting(SettingCreateRequest req) {
        Setting setting = Setting.builder()
                .settingKey(req.getSettingKey())
                .code(req.getCode())
                .description(req.getDescription())
                .defaultValue(req.getDefaultValue())
                .settingType(EnumUtils.resolveEnum(SettingType.class, req.getSettingType(), "Invalid SettingType"))
                .category(EnumUtils.resolveEnum(SettingCategory.class, req.getCategory(), "Invalid SettingCategory"))
                .permission(EnumUtils.resolveEnum(
                        SettingPermission.class, req.getPermission(), "Invalid SettingPermission"))
                .metadata(req.getMetadata())
                .displayOrder(req.getDisplayOrder())
                .build();
        setting.setStatus(StatusType.ACTIVE);
        return setting;
    }

    @NotNull
    private UserSettingHistory getUserSettingsHistory(
            SettingModifyRequest request, UserSetting userSetting, Setting setting) {
        UserSettingHistory history = new UserSettingHistory();
        history.setUserSetting(userSetting);
        history.setCustomerNo(userSetting.getCustomerNo());
        history.setAccountNo(userSetting.getAccountNo());
        history.setPhoneNo(userSetting.getPhoneNo());
        history.setCustomerKey(userSetting.getCustomerKey());
        history.setSettingKey(setting.getSettingKey());
        history.setOldSettingValue(userSetting.getSettingValue());
        history.setNewSettingValue(request.getSettingValue());
        history.setUpdateReason(request.getUpdateReason());
        history.setStatus(StatusType.MODIFIED);
        history.setMetadata(userSetting.getMetadata());
        return history;
    }

    @Transactional(readOnly = true)
    public SettingResponse getSettingBySettingKey(String settingKey) {
        Setting setting = settingRepository
                .findBySettingKeyAndActive(settingKey)
                .orElseThrow(() -> new BusinessException("Setting not found or disabled: " + settingKey));
        return settingMapper.toResponse(setting);
    }

    @Transactional(readOnly = true)
    public List<SettingResponse> getListSettings() {
        List<Setting> settings = settingRepository.findAllByStatus(StatusType.ACTIVE);
        return settings.stream().map(settingMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserSettingHistoryResponse> getUserSettingHistories(String customerKey) {
        log.info("Fetching active user setting histories for customerKey: {}", customerKey);
        List<UserSettingHistory> userSettingHistories = userSettingHistoryRepository.findByCustomerKey(customerKey);
        return userSettingHistories.stream()
                .map(userSettingHistoryMapper::toResponse)
                .toList();
    }
}
