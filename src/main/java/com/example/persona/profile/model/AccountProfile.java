package com.example.persona.profile.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "dgtl_account_profile")
public class AccountProfile extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_no")
    private String customerNo;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "service_sub_type")
    private String serviceSubType;

    @Column(name = "entry_type")
    private String entryType;

    @Column(name = "service_code")
    private String serviceCode;

    @Column(name = "class_of_service")
    private String classOfService;

    @Column(name = "account_category")
    private String accountCategory;

    @Column(name = "customer_segment")
    private String customerSegment;

    @Column(name = "meta_data")
    private String metaData;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "waive_fee")
    private Boolean waiveFee;

    @Column(name = "waive_fee_reason")
    private String waiveFeeReason;

    @Column(name = "transaction_max_limit", precision = 19, scale = 2)
    private BigDecimal transactionMaxLimit;

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
}
