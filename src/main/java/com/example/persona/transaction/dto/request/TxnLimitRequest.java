package com.example.persona.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TxnLimitRequest {
    @JsonProperty("limit_sn")
    private Long limitSn;

    @JsonProperty("master_account_no")
    private String masterAccountNo;

    @JsonProperty("customer_key")
    private String customerKey;

    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("account_no")
    private String accountNo;

    @JsonProperty("customer_no")
    private String customerNo;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("daily_limit")
    private BigDecimal dailyLimit;
}
