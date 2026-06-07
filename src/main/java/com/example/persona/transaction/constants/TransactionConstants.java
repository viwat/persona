package com.example.persona.transaction.constants;

/**
 * Constants for transaction-related operations. This class contains all
 * transaction-related constants used across the transaction service.
 */
public final class TransactionConstants {

    private TransactionConstants() {
        // Utility class - prevent instantiation
    }

    // Cache constants
    public static final String CACHE_NAME = "dailyTransactionAggregates";
    public static final String DAILY_AGG_KEY_FORMAT = "daily_agg:%s:%s:%s:%s";
    public static final int CACHE_EXPIRY_SECONDS = 86400;

    // Default values
    public static final String DEFAULT_BRANCH = "000";
    public static final String DEFAULT_TERMINAL = "MOBAPP";
    public static final String DEFAULT_DEVICE = "UNKNOWN";

    // MDC keys
    public static final String KEY_REQUEST_ID = "requestId";
    public static final String KEY_SESSION_ID = "sessionId";
}
