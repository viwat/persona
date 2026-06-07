package com.example.persona.profile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountProfileResponse {
    @JsonProperty("account_no")
    private String accountNo;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("account_class")
    private String accountClass;

    @JsonProperty("account_category")
    private String accountCategory;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("account_sub_type")
    private String accountSubType;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("account_open_date")
    private String accountOpenDate;

    @JsonProperty("class_of_service")
    private String classOfService;

    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("service_sub_type")
    private String serviceSubType;

    @JsonProperty("service_code")
    private String serviceCode;

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
