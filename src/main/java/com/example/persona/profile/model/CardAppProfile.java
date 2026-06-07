package com.example.persona.profile.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dgtl_card_app_profile")
public class CardAppProfile extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_app_id", nullable = false)
    private String customerAppId;

    @Column(name = "customer_no", nullable = false)
    private String customerNo;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "tracking_number", nullable = false)
    private String trackingNumber; // Card number or card reference

    @Column(name = "card_type")
    private String cardType; // DEBIT, CREDIT, PREPAID, etc.

    @Column(name = "card_brand")
    private String cardBrand; // VISA, MASTERCARD, RUPAY, etc.

    @Column(name = "card_name")
    private String cardName; // User-defined card name

    @Column(name = "card_status")
    private String cardStatus; // ACTIVE, INACTIVE, BLOCKED, EXPIRED, etc.

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "card_holder_name")
    private String cardHolderName;

    @Column(name = "css_number")
    private String cssNumber;

    @Column(name = "credit_limit", precision = 19, scale = 2)
    private BigDecimal creditLimit; // For credit cards

    @Column(name = "available_credit", precision = 19, scale = 2)
    private BigDecimal availableCredit; // For credit cards

    @Column(name = "transaction_max_limit", precision = 19, scale = 2)
    private BigDecimal transactionMaxLimit;

    @Column(name = "daily_limit", precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "is_hidden")
    private Boolean isHidden;

    @Column(name = "stop_transaction_notification")
    private Boolean stopTransactionNotification;

    @Column(name = "is_default_payment_card")
    private Boolean isDefaultPaymentCard;

    @Column(name = "allow_transaction")
    private Boolean allowTransaction; // true = allow transactions, false = view only

    @Column(name = "allow_online_transaction")
    private Boolean allowOnlineTransaction; // true = allow online transactions

    @Column(name = "allow_contactless_transaction")
    private Boolean allowContactlessTransaction; // true = allow contactless transactions

    @Column(name = "allow_international_transaction")
    private Boolean allowInternationalTransaction; // true = allow international transactions

    @Column(name = "card_icon")
    private String cardIcon; // Icon URL or icon code

    @Column(name = "card_color")
    private String cardColor; // Card color theme

    @Column(name = "meta_data", length = 2000)
    private String metaData; // JSON string for additional card information

    @Column(name = "version_key", nullable = false)
    private String versionKey; // Groups versions: customer_no + "_" + account_no + "_" + tracking_number

    @Column(name = "previous_version_id")
    private Long previousVersionId; // Links to previous version

    @Column(name = "checksum_sha256")
    private String checksumSha256;
}
