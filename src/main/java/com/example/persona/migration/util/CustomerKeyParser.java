package com.example.persona.migration.util;

import static com.example.persona.migration.constants.MigrationConstants.CUSTOMER_KEY_SEPARATOR;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

/**
 * Utility class for parsing customer keys in migration handlers.
 */
public final class CustomerKeyParser {

    private CustomerKeyParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Parses a customer key into its components.
     * Uses ACCOUNT_NO as default strategy for loginId.
     *
     * @param customerKey
     *            the customer key to parse
     * @return parsed key components
     */
    public static ParsedKey parse(String customerKey) {
        return parse(customerKey, LoginIdStrategy.ACCOUNT_NO);
    }

    /**
     * Parses a customer key into its components.
     *
     * @param customerKey
     *            the customer key to parse
     * @param loginIdStrategy
     *            strategy for determining loginId ("CUSTOMER_NO" or "ACCOUNT_NO")
     * @return parsed key components
     */
    public static ParsedKey parse(String customerKey, LoginIdStrategy loginIdStrategy) {
        if (!StringUtils.hasText(customerKey)) {
            return new ParsedKey(null, null, null, null);
        }

        if (customerKey.contains(CUSTOMER_KEY_SEPARATOR)) {
            // -1 limit keeps trailing empty segments (e.g. "999036090_" → ["999036090", ""])
            String[] parts = customerKey.split(CUSTOMER_KEY_SEPARATOR, -1);
            String customerNo = parts.length > 0 ? parts[0] : null;
            String accountNo = parts.length > 1 ? parts[1] : null;
            String masterAccId = parts.length > 2 && StringUtils.hasText(parts[2]) ? parts[2] : null;

            // loginId defaults to accountNo
            String loginId =
                    switch (loginIdStrategy) {
                        case CUSTOMER_NO -> customerNo;
                        case ACCOUNT_NO -> accountNo;
                    };

            return new ParsedKey(customerNo, accountNo, loginId, masterAccId);
        }

        // Format: raw "masterAccId" or "loginId"
        return new ParsedKey(null, null, customerKey, customerKey);
    }

    public enum LoginIdStrategy {
        CUSTOMER_NO,
        ACCOUNT_NO
    }

    /**
     * Parsed customer key components.
     *
     * @param customerNo
     *            the customer number
     * @param accountNo
     *            the account number
     * @param loginId
     *            the login ID
     * @param masterAccId
     *            the master account ID
     */
    public record ParsedKey(String customerNo, String accountNo, String loginId, String masterAccId) {

        @NotNull
        @Override
        public String toString() {
            return String.format(
                    "[customerNo=%s, accountNo=%s, loginId=%s, masterAccId=%s]",
                    customerNo, accountNo, loginId, masterAccId);
        }
    }
}
