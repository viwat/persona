package com.example.persona.transaction.dto;

import com.example.persona.transaction.model.TransactionLimit;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TxnLimitResponse {
    private Long limitSn;
    private String customerKey;
    private String serviceType;
    private String accountNo;
    private String customerNo;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private BigDecimal perTransactionLimit;
    private Integer dailyTransactionCount;
    private Integer monthlyTransactionCount;

    public static TxnLimitResponse from(TransactionLimit limit) {
        return TxnLimitResponse.builder()
                .limitSn(limit.getId())
                .customerKey(limit.getCustomerKey())
                .serviceType(limit.getServiceType())
                .accountNo(limit.getAccountNo())
                .customerNo(limit.getCustomerNo())
                .dailyLimit(limit.getDailyLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .perTransactionLimit(limit.getPerTransactionLimit())
                .dailyTransactionCount(limit.getDailyTransactionCount())
                .monthlyTransactionCount(limit.getMonthlyTransactionCount())
                .build();
    }
}
