package com.example.persona.favorite.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "dgtl_favorite",
        indexes = {@Index(name = "idx_favorite_service_type_code", columnList = "service_type, service_code")},
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_favorite_service_type_code",
                    columnNames = {"service_type", "service_code"})
        })
@Getter
@Setter
public class Favorite extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_type", nullable = false)
    private String serviceType; // e.g., TRANSFER, BILL_PAYMENT, TOP_UP

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "service_icon", nullable = false)
    private String serviceIcon;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "additional_data")
    private String additionalData;

    @Column(name = "context")
    private String context;

    @Column(name = "config_value1")
    private String configValue1;

    @Column(name = "config_value2")
    private String configValue2;

    @Column(name = "config_value3")
    private String configValue3;

    @Column(name = "config_value4")
    private String configValue4;

    @Column(name = "config_value5")
    private String configValue5;

    @Column(name = "config_value6")
    private String configValue6;

    @Column(name = "config_value7")
    private String configValue7;

    @Column(name = "config_value8")
    private String configValue8;

    @Column(name = "config_value9")
    private String configValue9;

    @Column(name = "config_value10")
    private String configValue10;
}
