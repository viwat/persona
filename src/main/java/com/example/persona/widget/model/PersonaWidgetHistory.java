package com.example.persona.widget.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "dgtl_persona_widget_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class PersonaWidgetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "widget_id", length = 255)
    private Long widgetId;

    @Column(name = "customer_no", length = 255)
    private String customerNo;

    @Column(name = "account_no", length = 255)
    private String accountNo;

    @Column(name = "phone_no", length = 255)
    private String phoneNo;

    @Column(name = "master_account_no", length = 255)
    private String masterAccountNo;

    @Column(name = "customer_key", length = 255)
    private String customerKey;

    @Column(name = "channel_code", length = 255)
    private String channelCode;

    @Column(name = "widget_code", length = 255)
    private String widgetCode;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "param1_key", length = 255)
    private String param1Key;

    @Column(name = "param1_value", length = 255)
    private String param1Value;

    @Column(name = "param2_key", length = 255)
    private String param2Key;

    @Column(name = "param2_value", length = 255)
    private String param2Value;

    @Column(name = "param3_key", length = 255)
    private String param3Key;

    @Column(name = "param3_value", length = 255)
    private String param3Value;

    @Column(name = "param4_key", length = 255)
    private String param4Key;

    @Column(name = "param4_value", length = 255)
    private String param4Value;

    @Column(name = "param5_key", length = 255)
    private String param5Key;

    @Column(name = "param5_value", length = 255)
    private String param5Value;

    @Column(name = "action", length = 50)
    @Enumerated(EnumType.STRING)
    private ActionType action;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "remarks", length = 500)
    private String remarks;

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }

    // Constructor to create history from PersonaWidget
    public PersonaWidgetHistory(PersonaWidget widget, ActionType action, String remarks) {
        this.widgetId = widget.getWidgetId();
        this.customerNo = widget.getCustomerNo();
        this.accountNo = widget.getAccountNo();
        this.phoneNo = widget.getPhoneNo();
        this.masterAccountNo = widget.getMasterAccountNo();
        this.customerKey = widget.getCustomerKey();
        this.channelCode = widget.getChannelCode();
        this.widgetCode = widget.getWidgetCode();
        this.displayOrder = widget.getDisplayOrder();
        this.param1Key = widget.getParam1Key();
        this.param1Value = widget.getParam1Value();
        this.param2Key = widget.getParam2Key();
        this.param2Value = widget.getParam2Value();
        this.param3Key = widget.getParam3Key();
        this.param3Value = widget.getParam3Value();
        this.param4Key = widget.getParam4Key();
        this.param4Value = widget.getParam4Value();
        this.param5Key = widget.getParam5Key();
        this.param5Value = widget.getParam5Value();
        this.action = action;
        this.remarks = remarks;
    }
}
