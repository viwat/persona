package com.example.persona.migration.constants;

/**
 * Constants used across the migration package.
 */
public final class MigrationConstants {

    private MigrationConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Channel codes
    public static final String CHANNEL_CODE_MOBAPP = "MOBAPP";

    /**
     * Oracle {@code DIGI_MASTER_ACCOUNT_V.APPLICATION_ID} value used for WingPay-bound
     * master rows. {@code LOGIN_ID} is not unique across applications; migration lookups
     * must scope by this id.
     */
    public static final String APPLICATION_ID_WINGPAY = "WINGPAY";

    // Device statuses
    public static final String DEVICE_STATUS_ACTIVE = "DA";

    // Stage codes
    public static final String STAGE_CODE_ACCOUNT = "ACCOUNT";
    public static final String STAGE_CODE_CARD = "CARD";
    public static final String STAGE_CODE_DEVICE = "DEVICE";
    public static final String STAGE_CODE_MASTER_ACCOUNT = "MASTER_ACCOUNT";

    // Error codes
    public static final String ERROR_CODE_ACCOUNT_ERROR = "ACCOUNT_ERROR";
    public static final String ERROR_CODE_CARD_ERROR = "CARD_ERROR";
    public static final String ERROR_CODE_DEVICE_ERROR = "DEVICE_ERROR";
    public static final String ERROR_CODE_MASTER_ACCOUNT_ERROR = "MASTER_ACCOUNT_ERROR";
    public static final String ERROR_CODE_ORACLE_DATA_NOT_FOUND = "ORACLE_DATA_NOT_FOUND";
    public static final String ERROR_CODE_NO_HANDLER = "NO_HANDLER";
    public static final String ERROR_CODE_EXCEPTION = "EXCEPTION";
    public static final String ERROR_CODE_STAGE_NOT_FOUND = "STAGE_NOT_FOUND";
    public static final String ERROR_CODE_STAGE_FAILED = "STAGE_FAILED";

    // Error messages
    public static final String ERROR_MESSAGE_UNKNOWN = "Unknown error";
    public static final String ERROR_MESSAGE_MISSING_ACCOUNT_NO = "Missing accountNo in customer key";

    // Customer key parsing
    public static final String CUSTOMER_KEY_SEPARATOR = "_";
}
