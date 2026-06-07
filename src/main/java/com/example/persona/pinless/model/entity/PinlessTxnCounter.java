package com.example.persona.pinless.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Entity
@Table(
        name = "pinless_txn_counter",
        indexes = {
            @Index(name = "idx_pinless_counter_customer", columnList = "customer_no"),
            @Index(name = "idx_pinless_counter_account", columnList = "account_id"),
            @Index(name = "idx_pinless_counter_resolved", columnList = "resolved_at DESC"),
            @Index(name = "idx_pinless_counter_channel", columnList = "channel"),
            @Index(name = "idx_pinless_counter_txn_type", columnList = "txn_type")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinlessTxnCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_no", nullable = false, length = 50)
    private String customerNo;

    @Column(name = "account_id", nullable = false, length = 64)
    private String accountId;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "txn_type", length = 50)
    private String txnType;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "auth_level_resolved", nullable = false, length = 20)
    private String authLevelResolved; // PINLESS or PIN

    @Column(name = "breach_reason", length = 50)
    private String breachReason; // null when PINLESS

    @Column(name = "resolved_at", nullable = false)
    private Instant resolvedAt;
}
