package com.example.persona.migration.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.persona.migration.util.CustomerKeyParser.LoginIdStrategy;
import com.example.persona.migration.util.CustomerKeyParser.ParsedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CustomerKeyParser Tests")
class CustomerKeyParserTest {

    @Nested
    @DisplayName("parse with default strategy (ACCOUNT_NO)")
    class ParseWithDefaultStrategy {

        @Test
        @DisplayName("should parse customer key with customerNo and accountNo")
        void shouldParseCustomerKeyWithCustomerNoAndAccountNo() {
            ParsedKey result = CustomerKeyParser.parse("999036090_099805807");

            assertThat(result.customerNo()).isEqualTo("999036090");
            assertThat(result.accountNo()).isEqualTo("099805807");
            assertThat(result.loginId()).isEqualTo("099805807");
            assertThat(result.masterAccId()).isNull();
        }

        @Test
        @DisplayName("should handle single value as raw key")
        void shouldHandleSingleValueAsRawKey() {
            ParsedKey result = CustomerKeyParser.parse("MA123456");

            assertThat(result.customerNo()).isNull();
            assertThat(result.accountNo()).isNull();
            assertThat(result.loginId()).isEqualTo("MA123456");
            assertThat(result.masterAccId()).isEqualTo("MA123456");
        }

        @Test
        @DisplayName("should handle empty customer key")
        void shouldHandleEmptyCustomerKey() {
            ParsedKey result = CustomerKeyParser.parse("");

            assertThat(result.customerNo()).isNull();
            assertThat(result.accountNo()).isNull();
            assertThat(result.loginId()).isNull();
            assertThat(result.masterAccId()).isNull();
        }

        @Test
        @DisplayName("should handle null customer key")
        void shouldHandleNullCustomerKey() {
            ParsedKey result = CustomerKeyParser.parse(null);

            assertThat(result.customerNo()).isNull();
            assertThat(result.accountNo()).isNull();
            assertThat(result.loginId()).isNull();
            assertThat(result.masterAccId()).isNull();
        }

        @Test
        @DisplayName("should handle whitespace-only customer key")
        void shouldHandleWhitespaceOnlyCustomerKey() {
            ParsedKey result = CustomerKeyParser.parse("   ");

            assertThat(result.customerNo()).isNull();
            assertThat(result.accountNo()).isNull();
            assertThat(result.loginId()).isNull();
        }
    }

    @Nested
    @DisplayName("parse with LoginIdStrategy.CUSTOMER_NO")
    class ParseWithCustomerNoStrategy {

        @Test
        @DisplayName("should set loginId to customerNo")
        void shouldSetLoginIdToCustomerNo() {
            ParsedKey result = CustomerKeyParser.parse("999036090_099805807", LoginIdStrategy.CUSTOMER_NO);

            assertThat(result.customerNo()).isEqualTo("999036090");
            assertThat(result.accountNo()).isEqualTo("099805807");
            assertThat(result.loginId()).isEqualTo("999036090");
        }
    }

    @Nested
    @DisplayName("parse with LoginIdStrategy.ACCOUNT_NO")
    class ParseWithAccountNoStrategy {

        @Test
        @DisplayName("should set loginId to accountNo")
        void shouldSetLoginIdToAccountNo() {
            ParsedKey result = CustomerKeyParser.parse("999036090_099805807", LoginIdStrategy.ACCOUNT_NO);

            assertThat(result.customerNo()).isEqualTo("999036090");
            assertThat(result.accountNo()).isEqualTo("099805807");
            assertThat(result.loginId()).isEqualTo("099805807");
        }

        @Test
        @DisplayName("should return null loginId when accountNo is missing")
        void shouldReturnNullLoginIdWhenAccountNoIsMissing() {
            ParsedKey result = CustomerKeyParser.parse("999036090_", LoginIdStrategy.ACCOUNT_NO);

            assertThat(result.customerNo()).isEqualTo("999036090");
            assertThat(result.accountNo()).isEmpty();
            assertThat(result.loginId()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle underscore-only customer key")
        void shouldHandleUnderscoreOnlyCustomerKey() {
            ParsedKey result = CustomerKeyParser.parse("_");

            assertThat(result.customerNo()).isEmpty();
            assertThat(result.accountNo()).isEmpty();
        }

        @Test
        @DisplayName("should handle multiple underscore separators")
        void shouldHandleMultipleUnderscoreSeparators() {
            ParsedKey result = CustomerKeyParser.parse("999036090_099805807_MA123456");

            assertThat(result.customerNo()).isEqualTo("999036090");
            assertThat(result.accountNo()).isEqualTo("099805807");
            assertThat(result.masterAccId()).isEqualTo("MA123456");
            assertThat(result.loginId()).isEqualTo("099805807");
        }
    }

    @Nested
    @DisplayName("ParsedKey toString")
    class ParsedKeyToString {

        @Test
        @DisplayName("should format toString correctly")
        void shouldFormatToStringCorrectly() {
            ParsedKey result = CustomerKeyParser.parse("999036090_099805807");

            String toString = result.toString();

            assertThat(toString).contains("customerNo=999036090");
            assertThat(toString).contains("accountNo=099805807");
            assertThat(toString).contains("loginId=099805807");
        }
    }
}
