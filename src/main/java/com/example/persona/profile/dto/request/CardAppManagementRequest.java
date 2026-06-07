package com.example.persona.profile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.dto.CustomerBaseRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class CardAppManagementRequest extends CustomerBaseRequest {

    @JsonProperty("card_no")
    private String cardNo;

    @JsonProperty("card_tracking_no")
    private String cardTrackingNo;

    @JsonProperty("card_name")
    private String cardName;

    @JsonProperty("card_holder_name")
    private String cardHolderName;

    @JsonProperty("css_number")
    private String cssNumber;

    @JsonProperty("card_type")
    private String cardType;

    @JsonProperty("card_brand")
    private String cardBrand;

    @JsonProperty("transaction_max_limit")
    private BigDecimal transactionMaxLimit;

    @JsonProperty("daily_limit")
    private BigDecimal dailyLimit;

    @JsonProperty("monthly_limit")
    private BigDecimal monthlyLimit;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("is_pinned")
    private Boolean isPinned;

    @JsonProperty("is_hidden")
    private Boolean isHidden;

    @JsonProperty("stop_transaction_notification")
    private Boolean stopTransactionNotification;

    @JsonProperty("is_default_payment_card")
    private Boolean isDefaultPaymentCard;

    @JsonProperty("allow_transaction")
    private Boolean allowTransaction;

    @JsonProperty("allow_online_transaction")
    private Boolean allowOnlineTransaction;

    @JsonProperty("allow_contactless_transaction")
    private Boolean allowContactlessTransaction;

    @JsonProperty("allow_international_transaction")
    private Boolean allowInternationalTransaction;

    @JsonProperty("card_icon")
    private String cardIcon;

    @JsonProperty("card_color")
    private String cardColor;

    @JsonProperty("card_status")
    private String cardStatus;

    @JsonProperty("expiry_date")
    private String expiryDate;

    @JsonProperty("created_on")
    private LocalDateTime createdOn;
}
