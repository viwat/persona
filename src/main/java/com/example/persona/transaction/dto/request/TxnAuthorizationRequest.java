package com.example.persona.transaction.dto.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TxnAuthorizationRequest {
    private Long authorizationSn;
    private String customerKey;
    private String accountNo;
    private String serviceType;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String maxAmountType;
    private String authenticationMethod;
}
