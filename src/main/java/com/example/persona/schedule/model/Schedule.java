package com.example.persona.schedule.model;

import com.example.persona.model.BaseModel;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "dgtl_schedule",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_service_type_code",
                    columnNames = {"service_type", "service_code"})
        },
        indexes = {
            @Index(name = "idx_schedule_service", columnList = "service_type, service_code"),
            @Index(name = "idx_schedule_type", columnList = "schedule_type")
        })
@Getter
@Setter
public class Schedule extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType;

    @Column(name = "service_code", nullable = false, length = 50)
    private String serviceCode;

    @Column(name = "schedule_type", nullable = false, length = 20)
    private String scheduleType;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "service_icon")
    private String serviceIcon;

    @Type(JsonType.class)
    @Column(name = "required_fields", columnDefinition = "json")
    private Map<String, Object> requiredFields;

    @Column(name = "min_amount", precision = 19, scale = 4)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 19, scale = 4)
    private BigDecimal maxAmount;

    @Column(name = "max_schedule_count")
    private Integer maxScheduleCount;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;

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
