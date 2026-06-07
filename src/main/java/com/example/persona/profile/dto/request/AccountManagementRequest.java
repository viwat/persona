package com.example.persona.profile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.dto.CustomerBaseRequest;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccountManagementRequest extends CustomerBaseRequest {

    @JsonProperty("account_no")
    private String accountNo;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("transaction_max_limit")
    private BigDecimal transactionMaxLimit;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("is_pinned")
    private Boolean isPinned;

    @JsonProperty("is_hidden")
    private Boolean isHidden;

    @JsonProperty("stop_transaction_notification")
    private Boolean stopTransactionNotification;

    @JsonProperty("is_default_payment_account")
    private Boolean isDefaultPaymentAccount;
}
