package com.example.persona.profile.model;

import com.example.persona.enums.ErrorCode;
import com.example.persona.exception.AppException;
import com.example.persona.model.BaseModel;
import com.example.persona.profile.converter.AccountBadgeListConverter;
import com.example.persona.profile.converter.AccountHolderListConverter;
import com.example.persona.profile.converter.AccountServiceListConverter;
import com.example.persona.profile.dto.response.AccountBadgeResponse;
import com.example.persona.profile.dto.response.AccountHolderResponse;
import com.example.persona.profile.dto.response.AccountServiceResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dgtl_account_app_profile")
public class AccountAppProfile extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Business customer key (composite or login id). Persisted under legacy column name
     * {@code customer_app_id} — do not rename the column in DB.
     */
    @Column(name = "customer_app_id", nullable = false)
    private String customerKey;

    @Column(name = "customer_no")
    private String customerNo;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "account_hash", nullable = false, length = 64)
    private String accountHash; // SHA-256 hash of customer_no + account_no for URL usage

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "account_class")
    private String accountClass;

    @Column(name = "class_of_service")
    private String classOfService;

    @Column(name = "account_category")
    private String accountCategory;

    @Column(name = "account_segment")
    private String accountSegment;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "balance", precision = 19, scale = 2)
    private java.math.BigDecimal balance;

    @Column(name = "meta_data")
    private String metaData;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "is_hidden")
    private Boolean isHidden;

    @Column(name = "stop_transaction_notification")
    private Boolean stopTransactionNotification;

    @Column(name = "is_default_payment_account")
    private Boolean isDefaultPaymentAccount;

    @Column(name = "allow_transaction")
    private Boolean allowTransaction; // true = allow transactions, false = view only

    @Column(name = "allow_card_usage")
    private Boolean allowCardUsage; // true = allow card usage, false = no card usage

    @Column(name = "account_holders", length = 2000)
    @Convert(converter = AccountHolderListConverter.class)
    private List<AccountHolderResponse> accountHolders; // JSON list of account holders for joint accounts

    @Column(name = "account_services", length = 2000)
    @Convert(converter = AccountServiceListConverter.class)
    private List<AccountServiceResponse> accountServices; // JSON list of linked services (Visa, Mastercard, UPI, etc.)

    @Column(name = "account_badges", length = 2000)
    @Convert(converter = AccountBadgeListConverter.class)
    private List<AccountBadgeResponse> accountBadges; // JSON list of custom badges

    @Column(name = "version_key", nullable = false)
    private String versionKey; // Groups versions: customer_no + "_" + account_no

    @Column(name = "previous_version_id")
    private Long previousVersionId; // Links to previous version

    @Column(name = "checksum_sha256")
    private String checksumSha256;

    /**
     * Generates a SHA-256 hash fromEntity customer_no and account_no. This hash is
     * used in URLs instead of exposing the actual account number.
     */
    public String generateAccountHash() {
        if (customerNo == null || accountNo == null) {
            throw new IllegalStateException("Customer number and account number must be set before generating hash");
        }
        String content = customerNo + "_" + accountNo;
        return sha256(content);
    }

    /**
     * Generates SHA-256 hash of the given content.
     */
    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new AppException("SHA-256 algorithm not available", ErrorCode.VALIDATION_ERROR);
        }
    }
}
