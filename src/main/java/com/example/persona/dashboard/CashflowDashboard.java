package com.example.persona.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashflowDashboard {
    private CashflowSummary summary;
    private List<CashflowPeriod> periods;
    private List<CashflowTransaction> transactions;
    private TimeRange selectedRange;

    @Data
    @Builder
    public static class CashflowSummary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private Currency selectedCurrency;
    }

    @Data
    @Builder
    public static class CashflowPeriod {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal income;
        private BigDecimal expense;
        private String periodLabel; // e.g., "1-7", "8-14"
    }

    @Data
    @Builder
    public static class CashflowTransaction {
        private String id;
        private TransactionType type;
        private String description;
        private BigDecimal amount;
        private LocalDate date;
        private TransactionCategory category;
    }

    public enum TimeRange {
        WEEK("1week"),
        MONTH("1month"),
        SIX_MONTHS("6month"),
        YEAR("1year");

        private final String value;

        TimeRange(String value) {
            this.value = value;
        }
    }

    public enum Currency {
        USD("US Dollar"),
        KHR("KH Riel");

        private final String label;

        Currency(String label) {
            this.label = label;
        }
    }

    public enum TransactionType {
        INCOME,
        EXPENSE
    }

    public enum TransactionCategory {
        SALARY("Salary"),
        TRANSFER("Transfer"),
        RECEIVE("Receive");

        private final String displayName;

        TransactionCategory(String displayName) {
            this.displayName = displayName;
        }
    }
}
