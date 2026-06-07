package com.example.persona.transaction.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dgtl_transaction_authorization")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAuthorization extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToOne
    @JoinColumn(name = "payment_channel_id", nullable = false)
    private PaymentChannel paymentChannel;

    @ManyToOne
    @JoinColumn(name = "auth_threshold", nullable = false)
    private AuthenticationThreshold authenticationThreshold;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "max_amount_type", nullable = false)
    private String maxAmountType;

    @Column(name = "data", nullable = false)
    private String data;

    @Column(name = "metadata")
    private String metadata;
}
