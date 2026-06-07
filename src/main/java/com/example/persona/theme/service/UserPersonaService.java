package com.example.persona.theme.service;

import com.example.persona.cache.UserPersonaCache;
import com.example.persona.dto.PersonaChangeRequest;
import com.example.persona.dto.response.CustomerPersonaResponse;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.model.CustomerPersona;
import com.example.persona.theme.dto.request.ThemeCustomizationRequest;
import com.example.persona.theme.dto.request.UserPersonaCreateRequest;
import com.example.persona.theme.dto.response.UserPersonaResponse;
import com.example.persona.theme.mapper.UserPersonaMapper;
import com.example.persona.theme.model.UserPersona;
import com.example.persona.theme.model.UserPersonaHistory;
import com.example.persona.theme.repository.UserPersonaHistoryRepository;
import com.example.persona.theme.repository.UserPersonaRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPersonaService {
    private final UserPersonaMapper userPersonaMapper;
    private final ThemeService themeService;
    private final UserPersonaCache userPersonaCache;
    private final UserPersonaRepository userPersonaRepository;
    private final UserPersonaHistoryRepository userPersonaHistoryRepository;

    @Transactional
    public UserPersonaResponse createUserPersona(UserPersonaCreateRequest request) {
        log.info("Creating user persona for customerKey: {}", request.getCustomerKey());
        userPersonaRepository.findByCustomerKey(request.getCustomerKey()).ifPresent(up -> {
            throw new BusinessException("UserPersona is already existed with customerKey: " + up.getCustomerKey());
        });
        UserPersona userPersona = userPersonaMapper.toEntity(request);
        userPersona.setStatus(StatusType.ACTIVE);
        userPersona.setCreatedDate(LocalDateTime.now());
        userPersonaRepository.save(userPersona);
        return userPersonaMapper.toResponse(userPersona);
    }

    @Transactional
    public CustomerPersonaResponse customizePersona(String customerKey, ThemeCustomizationRequest request) {
        log.info("Customizing persona for customerKey: {}", customerKey);

        themeService.getActiveAccentColors().stream()
                .filter(color -> color.getColorCode().equals(request.getAccentColor()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Invalid accent color: " + request.getAccentColor()));

        // Find existing or create new UserPersona
        UserPersona userPersona = userPersonaRepository
                .findByCustomerKey(customerKey)
                .orElseThrow(() -> new BusinessException("UserPersona is not found with customerKey: " + customerKey));
        userPersona.setAppearance(request.getAppearance().toUpperCase());
        userPersona.setTextSize(request.getTextSize().toUpperCase());
        userPersona.setVersion(userPersona.getVersion() == null ? 1 : userPersona.getVersion() + 1);
        userPersonaRepository.save(userPersona);

        userPersonaCache.cacheUserPersona(customerKey, userPersonaMapper.toResponse(userPersona));
        return userPersonaMapper.toCustomerPersonaResponse(userPersona);
    }

    public CustomerPersonaResponse getCurrentPersona(String customerKey) {
        CustomerPersona persona =
                userPersonaCache.getCustomerPersona(customerKey).orElse(null);
        if (Objects.isNull(persona)) {
            UserPersona userPersona = userPersonaRepository
                    .findByCustomerKey(customerKey)
                    .orElseThrow(
                            () -> new BusinessException("UserPersona is not found with customerKey: " + customerKey));
            userPersonaCache.cacheUserPersona(customerKey, userPersonaMapper.toResponse(userPersona));
            return userPersonaMapper.toCustomerPersonaResponse(userPersona);
        }
        return userPersonaMapper.toCustomerPersonaResponse(persona);
    }

    @Transactional
    public CustomerPersonaResponse changePersona(PersonaChangeRequest request) {
        // Get current persona
        UserPersona currentPersona = userPersonaRepository
                .findByCustomerKey(request.getCustomerKey())
                .orElseThrow(() -> new BusinessException(
                        "UserPersona is not found with customerKey: " + request.getCustomerKey()));

        // Create comprehensive history record with all relevant fields
        UserPersonaHistory history = UserPersonaHistory.builder()
                .customerKey(request.getCustomerKey())
                .previousThemeCode(currentPersona.getThemeCode())
                .previousPersonaCode(currentPersona.getPersonaCode())
                .previousAccentColor(currentPersona.getAccentColor())
                .previousAppearance(currentPersona.getAppearance())
                .previousTextSize(currentPersona.getTextSize())
                .previousVersion(currentPersona.getVersion())
                .modifiedAt(LocalDateTime.now()) // Add reason for change if available in request
                .changedBy(request.getCustomerKey()) // Add user who made the change
                .build();

        // Save history before making changes
        userPersonaHistoryRepository.save(history);

        // Update persona with new values using a separate method for better
        // maintainability
        updatePersonaAttributes(currentPersona, request);

        // Increment version
        currentPersona.setVersion(currentPersona.getVersion() + 1);

        // Save updated persona
        UserPersona updatedPersona = userPersonaRepository.save(currentPersona);
        CustomerPersona persona = userPersonaCache
                .cacheCustomerPersona(request.getCustomerKey(), userPersonaMapper.toCustomerPersona(updatedPersona))
                .orElse(null);

        return userPersonaMapper.toCustomerPersonaResponse(persona);
    }

    private void updatePersonaAttributes(UserPersona currentPersona, PersonaChangeRequest request) {
        if (request.getThemeCode() != null) {
            themeService.validateThemeCode(request.getThemeCode());
            currentPersona.setThemeCode(request.getThemeCode());
        }
        if (request.getPersonaCode() != null) {
            validatePersonaCode(request.getPersonaCode()); // Add validation method
            currentPersona.setPersonaCode(request.getPersonaCode());
        }
        // Add other attribute updates as needed
    }

    private void validatePersonaCode(String personaCode) {
        // Implement persona code validation logic
    }
}
