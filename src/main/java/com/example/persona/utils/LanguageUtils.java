package com.example.persona.utils;

import com.example.persona.config.LocaleContextHolder;
import com.example.persona.model.MultilingualField;
import org.springframework.stereotype.Component;

@Component
public class LanguageUtils {

    /**
     * Gets the localized text based on user's locale
     *
     * @param multilingualField
     *            Object containing multilingual texts
     * @return The text in appropriate language, defaults to English if translation
     *         not found
     */
    public static String getLocalizedText(MultilingualField multilingualField) {
        if (multilingualField == null) {
            return null;
        }
        String languageCode = LocaleContextHolder.getLocale();
        if (languageCode == null) {
            languageCode = multilingualField.getEn();
        }

        return switch (languageCode) {
            case "km" -> getTextWithFallback(multilingualField.getKm(), multilingualField.getEn());
            case "zh" -> getTextWithFallback(multilingualField.getZh(), multilingualField.getEn());
            default -> multilingualField.getEn();
        };
    }

    /**
     * Returns the localizedText if not null, otherwise returns fallback text
     */
    private static String getTextWithFallback(String localizedText, String fallback) {
        return localizedText != null && !localizedText.trim().isEmpty() ? localizedText : fallback;
    }

    /**
     * Gets the current language code fromEntity the LocaleContext
     *
     * @return The lowercase 2-letter language code (e.g. "en", "km", "zh")
     */
    public static String getLanguage() {
        return LocaleContextHolder.getLocale();
    }
}
