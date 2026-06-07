package com.example.persona.transaction.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dgtl_payment_channel")
@Getter
@Setter
public class PaymentChannel extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_code", nullable = false, unique = true)
    private String channelCode;

    @Column(name = "channel_name", nullable = false)
    private String channelName;

    @Column(name = "description")
    private String description;
}
