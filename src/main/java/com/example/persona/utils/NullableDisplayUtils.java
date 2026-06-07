package com.example.persona.utils;

import org.springframework.util.StringUtils;

/**
 * Display fallbacks for profile API JSON: {@code null} and blank strings become {@value NOT_AVAILABLE}.
 */
public final class NullableDisplayUtils {

    public static final String NOT_AVAILABLE = "N/A";

    private NullableDisplayUtils() {}

    public static String stringOrNa(String value) {
        return StringUtils.hasText(value) ? value.trim() : NOT_AVAILABLE;
    }
}
