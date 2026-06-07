package com.example.persona.transaction.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "dgtl_transaction_detail")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TransactionDetail extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "customer_no", nullable = false)
    private String customerNo;

    @Column(name = "branch_code", nullable = false)
    private String branchCode;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "phone_no", nullable = false)
    private String phoneNo;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "master_account_id", nullable = false)
    private String masterAccountId;

    @Column(name = "master_account_no", nullable = false)
    private String masterAccountNo;

    @Column(name = "payment_via", length = 500)
    private String paymentVia;

    @Column(name = "payment_method", length = 500)
    private String paymentMethod;

    @Column(name = "payment_channel", length = 500)
    private String paymentChannel;

    @Column(name = "transaction_auth_code", length = 500)
    private String transactionAuthCode;

    @Column(name = "auth_method", length = 500)
    private String authMethod;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "service_type", length = 500)
    private String serviceType;

    @Column(name = "error_code", length = 500)
    private String errorCode;

    @Column(name = "dev_error_code", length = 500)
    private String devErrorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "dev_error_message", length = 500)
    private String devErrorMessage;

    @Column(name = "trace_id", nullable = false)
    private String traceId;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "sub_transaction_id", nullable = false)
    private String subTransactionId;

    @NotNull
    @Column(name = "transaction_reference", unique = true)
    private String transactionReference;

    @NotNull
    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "transaction_type")
    private String type;

    @ElementCollection
    @CollectionTable(name = "dgtl_transaction_reactions", joinColumns = @JoinColumn(name = "transaction_id"))
    @Column(name = "reaction_emoji")
    private Set<String> reactionEmojis;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "gift_format")
    private String giftFormat;

    @Column(name = "metadata", length = 500)
    private String metadata;

    @Column(name = "config_attr1", length = 500)
    private String configAttr1;

    @Column(name = "config_attr2", length = 500)
    private String configAttr2;

    @Column(name = "config_attr3", length = 500)
    private String configAttr3;

    @Column(name = "config_attr4", length = 500)
    private String configAttr4;

    @Column(name = "config_attr5", length = 500)
    private String configAttr5;

    @Column(name = "config_attr6", length = 500)
    private String configAttr6;

    @Column(name = "config_attr7", length = 500)
    private String configAttr7;

    @Column(name = "config_attr8", length = 500)
    private String configAttr8;

    @Column(name = "config_attr9", length = 500)
    private String configAttr9;

    @Column(name = "config_attr10", length = 500)
    private String configAttr10;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "day")
    private String day;

    @Column(name = "month")
    private String month;

    @Column(name = "year")
    private String year;
}
