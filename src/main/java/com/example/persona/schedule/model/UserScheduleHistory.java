package com.example.persona.schedule.model;

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
        name = "dgtl_user_schedule_history",
        indexes = {
            @Index(name = "idx_history_user_schedule", columnList = "user_schedule_id"),
            @Index(name = "idx_history_execution", columnList = "last_execution_datetime"),
            @Index(name = "idx_history_status", columnList = "schedule_status")
        })
@Getter
@Setter
public class UserScheduleHistory extends BaseCustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_schedule_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_history_user_schedule"))
    private UserSchedule userSchedule;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(name = "schedule_name", nullable = false)
    private String scheduleName;

    @Column(name = "schedule_type", nullable = false)
    private String scheduleType;

    @Column(name = "frequency", nullable = false)
    private String frequency; // DAILY, WEEKLY, MONTHLY, YEARLY

    @Column(name = "frequency_detail")
    private String frequencyDetail; // JSON: for weekly: [1,3,5], for monthly: {"day": 15}

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

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

    @Column(name = "schedule_status")
    @Enumerated(EnumType.STRING)
    private ScheduleStatus scheduleStatus; // ACTIVE, PAUSED, COMPLETED, FAILED

    @Column(name = "action", nullable = false)
    private String action;

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
