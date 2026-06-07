package com.example.persona.config;

public class LocaleContextHolder {
    private static final ThreadLocal<String> userLocale = new ThreadLocal<>();

    public static void setLocale(String locale) {
        userLocale.set(locale);
    }

    public static String getLocale() {
        return userLocale.get() == null ? "en" : userLocale.get();
    }

    public static void clear() {
        userLocale.remove();
    }
}
