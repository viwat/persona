package com.example.persona.utils;

import com.example.persona.config.properties.SystemProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisKeyUtils {
    private final SystemProperties environment;

    final String TRANSLATIONS_KEY = "TRANSLATION";
    final String SETTING_KEY = "SETTING";

    private String getEnvironment() {
        return environment.getEnv();
    }

    public String getTranslationsKey(String appCode) {
        return getEnvironment() + ":APPCODE:" + appCode + ":" + TRANSLATIONS_KEY;
    }

    public String getPublicSettingKey() {
        return getEnvironment() + ":USER:GLOBAL:" + SETTING_KEY;
    }

    public String getUserSettingKey(String customerKey) {
        return getEnvironment() + ":USER:" + customerKey + ":" + SETTING_KEY;
    }

    public String getUserSettingKey(Long userScheduleId, String customerKey) {
        return getEnvironment() + ":USER:" + customerKey + ":" + SETTING_KEY + ":" + userScheduleId;
    }
}
