package com.example.persona.profile.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dgtl_account_app_service_limit")
public class AccountAppServiceLimit extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_app_id", nullable = false)
    private String customerAppId;

    @Column(name = "customer_no", nullable = false)
    private String customerNo;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "service_type", nullable = false)
    private String serviceType; // TRANSFER, PAYMENT, WITHDRAWAL, etc.

    @Column(name = "service_code")
    private String serviceCode; // Optional: specific service code

    @Column(name = "transaction_max_limit", precision = 19, scale = 2)
    private BigDecimal transactionMaxLimit;

    @Column(name = "daily_limit", precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "per_transaction_limit", precision = 19, scale = 2)
    private BigDecimal perTransactionLimit;

    @Column(name = "version_key", nullable = false)
    private String versionKey; // Groups versions: customer_no + "_" + account_no + "_" + service_type

    @Column(name = "previous_version_sn")
    private Long previousVersionSn; // Links to previous version

    @Column(name = "checksum_sha256")
    private String checksumSha256;
}
