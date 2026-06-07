package com.example.persona.setting.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.persona.enums.SettingCategory;
import com.example.persona.enums.SettingPermission;
import com.example.persona.enums.SettingType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SettingTest {

    @Test
    void testNoArgsConstructorAndSettersGetters() {
        Setting setting = new Setting();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        setting.setId(1L);
        setting.setSettingType(SettingType.STRING);
        setting.setDescription("description");
        setting.setCode("CODE_1");
        setting.setSettingKey("KEY_1");
        setting.setDefaultValue("default");
        setting.setCategory(SettingCategory.GENERAL);
        setting.setPermission(SettingPermission.RESTRICTED);
        setting.setDisplayOrder(10);
        setting.setMetadata(metadata);
        setting.setCallbackUrl("http://callback");

        assertEquals(1L, setting.getId());
        assertEquals(SettingType.STRING, setting.getSettingType());
        assertEquals("description", setting.getDescription());
        assertEquals("CODE_1", setting.getCode());
        assertEquals("KEY_1", setting.getSettingKey());
        assertEquals("default", setting.getDefaultValue());
        assertEquals(SettingCategory.GENERAL, setting.getCategory());
        assertEquals(SettingPermission.RESTRICTED, setting.getPermission());
        assertEquals(10, setting.getDisplayOrder());
        assertEquals(metadata, setting.getMetadata());
        assertEquals("http://callback", setting.getCallbackUrl());
    }

    @Test
    void testAllArgsConstructor() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        Setting setting = new Setting(
                2L,
                SettingType.NUMBER,
                "desc",
                "CODE_2",
                "KEY_2",
                "100",
                SettingCategory.PRIVACY_AND_SECURITY,
                SettingPermission.USER_OVERRIDE,
                5,
                metadata,
                "http://cb");

        assertEquals(2L, setting.getId());
        assertEquals(SettingType.NUMBER, setting.getSettingType());
        assertEquals("desc", setting.getDescription());
        assertEquals("CODE_2", setting.getCode());
        assertEquals("KEY_2", setting.getSettingKey());
        assertEquals("100", setting.getDefaultValue());
        assertEquals(SettingCategory.PRIVACY_AND_SECURITY, setting.getCategory());
        assertEquals(SettingPermission.USER_OVERRIDE, setting.getPermission());
        assertEquals(5, setting.getDisplayOrder());
        assertEquals(metadata, setting.getMetadata());
        assertEquals("http://cb", setting.getCallbackUrl());
    }

    @Test
    void testBuilder() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        Setting setting = Setting.builder()
                .id(3L)
                .settingType(SettingType.BOOLEAN)
                .description("builder-desc")
                .code("CODE_3")
                .settingKey("KEY_3")
                .defaultValue("true")
                .category(SettingCategory.GENERAL)
                .permission(SettingPermission.RESTRICTED)
                .displayOrder(1)
                .metadata(metadata)
                .callbackUrl("http://builder")
                .build();

        assertEquals(3L, setting.getId());
        assertEquals(SettingType.BOOLEAN, setting.getSettingType());
        assertEquals("builder-desc", setting.getDescription());
        assertEquals("CODE_3", setting.getCode());
        assertEquals("KEY_3", setting.getSettingKey());
        assertEquals("true", setting.getDefaultValue());
        assertEquals(SettingCategory.GENERAL, setting.getCategory());
        assertEquals(SettingPermission.RESTRICTED, setting.getPermission());
        assertEquals(1, setting.getDisplayOrder());
        assertEquals(metadata, setting.getMetadata());
        assertEquals("http://builder", setting.getCallbackUrl());
    }
}
