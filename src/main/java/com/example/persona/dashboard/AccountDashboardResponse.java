package com.example.persona.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccountDashboardResponse {
    private DashboardSummary summary;
    private List<AccountInfo> accounts;

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DashboardSummary {
        private BigDecimal totalBalanceUSD;
        private BigDecimal totalBalanceKHR;
        private int totalAccounts;
    }

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AccountInfo {
        private String accountId;
        private String accountName;
        private AccountType accountType;
        private String accountNumber;
        private BigDecimal balance;
        private Currency currency;
        private AccountStatus status;
        private boolean primaryDefault;
        private boolean secondaryDefault;
        private boolean hidden;
        private Set<PaymentMethod> supportedPaymentMethods;
    }

    public enum AccountType {
        SAVING,
        CURRENT,
        JOINT_SAVING,
        GOALS,
        TERM_DEPOSIT
    }

    public enum Currency {
        USD,
        KHR
    }

    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        DORMANT,
        BLOCKED
    }

    public enum PaymentMethod {
        VISA,
        MASTERCARD,
        FCC,
        BAKONG,
        NHAM24,
        QR
    }
}
