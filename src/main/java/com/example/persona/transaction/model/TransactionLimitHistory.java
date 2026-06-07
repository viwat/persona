package com.example.persona.transaction.model;

import com.example.persona.enums.AccountType;
import com.example.persona.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "dgtl_transaction_limit_history")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLimitHistory extends BaseModel {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_limit_sn", nullable = false)
    private Long transactionLimitSn;

    @Column(name = "customer_key", nullable = false)
    private String customerKey;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "customer_no", nullable = false)
    private String customerNo;

    @Column(name = "master_account_id", nullable = false)
    private String masterAccountId;

    @Column(name = "master_account_no", nullable = false)
    private String masterAccountNo;

    @Column(name = "channel_code", nullable = false)
    private String channelCode;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "daily_limit", nullable = false)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", nullable = false)
    private BigDecimal monthlyLimit;

    @Column(name = "per_transaction_limit", nullable = false)
    private BigDecimal perTransactionLimit;

    @Column(name = "daily_transaction_count")
    private Integer dailyTransactionCount;

    @Column(name = "monthly_transaction_count")
    private Integer monthlyTransactionCount;

    @Column(name = "data")
    private String data;

    @Column(name = "metadata")
    private String metadata;
}
