package com.example.persona.transaction.dto;

import com.example.persona.transaction.model.TransactionAuthorization;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TxnAuthorizeResponse {
    private String customerKey;
    private String accountNo;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String maxAmountType;

    public static TxnAuthorizeResponse from(TransactionAuthorization authorization) {
        return TxnAuthorizeResponse.builder()
                .customerKey(authorization.getCustomerKey())
                .accountNo(authorization.getAccountNo())
                .minAmount(authorization.getMinAmount())
                .maxAmount(authorization.getMaxAmount())
                .build();
    }
}
