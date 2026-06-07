package com.example.persona.theme.validator;

import com.example.persona.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class ThemeValidator {
    public void validateThemeCode(String themeCode) {
        if (!StringUtils.hasText(themeCode)) {
            throw new BusinessException("Theme code cannot be empty");
        }
        if (!themeCode.matches("^[a-zA-Z0-9-_]{1,50}$")) {
            throw new BusinessException("Invalid theme code format");
        }
    }
}
