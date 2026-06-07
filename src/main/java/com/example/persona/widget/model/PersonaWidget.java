package com.example.persona.widget.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dgtl_persona_widget")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonaWidget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "widget_id", length = 255)
    private Long widgetId;

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
}
