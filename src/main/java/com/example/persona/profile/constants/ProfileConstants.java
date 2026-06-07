package com.example.persona.profile.constants;

/**
 * Constants used across the profile package.
 */
public final class ProfileConstants {

    private ProfileConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Account statuses
    public static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
    public static final String ACCOUNT_STATUS_INACTIVE = "INACTIVE";

    // Display order
    public static final Integer DEFAULT_PINNED_DISPLAY_ORDER = 0;
    public static final Integer DEFAULT_DISPLAY_ORDER = 999;

    // Version key separator
    public static final String VERSION_KEY_SEPARATOR = "_";

    // Channel codes
    public static final String CHANNEL_CODE_MOBAPP = "MOBAPP";
}
