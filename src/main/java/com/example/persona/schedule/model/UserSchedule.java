package com.example.persona.schedule.model;

import com.example.persona.enums.FrequencyType;
import com.example.persona.model.BaseCustomerEntity;
import com.example.persona.schedule.enums.ScheduleStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "dgtl_user_schedule",
        indexes = {
            @Index(name = "idx_next_execution", columnList = "next_execution_datetime"),
            @Index(name = "idx_schedule_status", columnList = "schedule_status"),
            @Index(name = "idx_customer_status", columnList = "customer_key, schedule_status"),
            @Index(name = "idx_schedule_fk", columnList = "schedule_id")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_customer_reference_active",
                    columnNames = {"customer_key", "reference_id", "schedule_status"})
        })
@Getter
@Setter
public class UserSchedule extends BaseCustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_schedule_schedule"))
    private Schedule schedule;

    @Column(name = "schedule_id", nullable = false, insertable = false, updatable = false)
    private Long scheduleId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(name = "schedule_name", nullable = false)
    private String scheduleName;

    @Column(name = "schedule_type", nullable = false)
    private String scheduleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private FrequencyType frequency;

    @Column(name = "frequency_detail")
    private String frequencyDetail;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "next_execution_datetime")
    private LocalDateTime nextExecutionDateTime;

    @Column(name = "last_execution_datetime")
    private LocalDateTime lastExecutionDateTime;

    @Column(name = "execution_count")
    private Integer executionCount;

    @Column(name = "max_execution_count")
    private Integer maxExecutionCount;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "reference_id", nullable = false)
    private String referenceId; // Account number, biller code, etc.

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "schedule_status")
    @Enumerated(EnumType.STRING)
    private ScheduleStatus scheduleStatus; // ACTIVE, PAUSED, COMPLETED, FAILED

    @Column(name = "additional_data")
    private String additionalData; // JSON string for additional data

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
