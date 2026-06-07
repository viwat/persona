package com.example.persona.enums;

public enum SettingPermission {
    GLOBAL_ONLY, // Can only be set globally by admin
    USER_OVERRIDE, // Global setting that can be overridden by users
    USER_ONLY, // Individual user setting only
    RESTRICTED // Cannot be changed (read-only)
}
