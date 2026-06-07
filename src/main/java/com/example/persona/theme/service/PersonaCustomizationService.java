package com.example.persona.theme.service;

import com.example.persona.exception.BusinessException;
import com.example.persona.theme.dto.request.ThemeCustomizationRequest;
import com.example.persona.theme.model.Theme;
import com.example.persona.theme.model.UserPersona;
import com.example.persona.theme.repository.ThemeRepository;
import com.example.persona.theme.repository.UserPersonaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonaCustomizationService {

    private final UserPersonaRepository userPersonaRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public UserPersona customizeTheme(String customerNo, ThemeCustomizationRequest request) {
        log.info("Customizing theme for customer: {}", customerNo);

        UserPersona userPersona =
                userPersonaRepository.findByCustomerNo(customerNo).orElseGet(() -> createNewUserPersona(customerNo));

        Theme selectedTheme = themeRepository
                .findById(request.getThemeId())
                .orElseThrow(() -> new BusinessException("Theme not found with id: " + request.getThemeId()));

        updateUserPersona(userPersona, selectedTheme, request);

        return userPersonaRepository.save(userPersona);
    }

    private UserPersona createNewUserPersona(String customerNo) {
        UserPersona newUserPersona = new UserPersona();
        newUserPersona.setCustomerNo(customerNo);
        newUserPersona.setVersion(1);
        newUserPersona.setStatus(com.example.persona.enums.StatusType.ACTIVE);
        return newUserPersona;
    }

    private void updateUserPersona(UserPersona userPersona, Theme theme, ThemeCustomizationRequest request) {
        userPersona.setThemeCode(theme.getId().toString());
        userPersona.setThemeCategory(theme.getCategory().getCode());
        userPersona.setAccentColor(request.getAccentColor());
        userPersona.setAppearance(request.getAppearance());
        userPersona.setVersion(userPersona.getVersion() != null ? userPersona.getVersion() + 1 : 1);

        // Copy theme metadata if exists
        if (theme.getMetadata() != null) {
            userPersona.setMetadata(theme.getMetadata());
        }
    }
}
