package com.example.persona.transaction.model;

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

@Entity
@Table(name = "dgtl_transaction_authorization_history")
@Getter
@Setter
public class TransactionAuthorizationHistory extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_authorization_id", nullable = false)
    private Long transactionAuthorizationSn;

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

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "payment_channel", nullable = false)
    private String paymentChannel;

    @Column(name = "auth_threshold", nullable = false)
    private String authThreshold;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "data", nullable = false)
    private String data;

    @Column(name = "metadata")
    private String metadata;
}
