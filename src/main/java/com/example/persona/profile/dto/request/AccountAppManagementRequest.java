package com.example.persona.profile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.profile.dto.response.AccountBadgeResponse;
import com.example.persona.profile.dto.response.AccountHolderResponse;
import com.example.persona.profile.dto.response.AccountServiceResponse;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccountAppManagementRequest extends CustomerBaseRequest {

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

    @JsonProperty("allow_transaction")
    private Boolean allowTransaction;

    @JsonProperty("allow_card_usage")
    private Boolean allowCardUsage;

    @JsonProperty("account_holders")
    private List<AccountHolderResponse> accountHolders;

    @JsonProperty("account_services")
    private List<AccountServiceResponse> accountServices;

    @JsonProperty("account_badges")
    private List<AccountBadgeResponse> accountBadges;

    @JsonProperty("account_class")
    private String accountClass;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("account_category")
    private String accountCategory;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("class_of_service")
    private String classOfService;

    @JsonProperty("account_holder_name")
    private String accountHolderName;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("balance")
    private BigDecimal balance;
}
