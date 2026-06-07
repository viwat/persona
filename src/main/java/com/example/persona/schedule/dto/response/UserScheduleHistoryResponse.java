package com.example.persona.schedule.dto.response;

import com.example.persona.enums.FrequencyType;
import com.example.persona.schedule.enums.ScheduleStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserScheduleHistoryResponse {
    private Long id;
    private Long scheduleId;
    private Long userScheduleId;
    private String serviceType;
    private String serviceCode;
    private String scheduleName;
    private String scheduleType;
    private FrequencyType frequency;
    private String frequencyDetail;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime nextExecutionDateTime;
    private LocalDateTime lastExecutionDateTime;
    private Integer executionCount;
    private Integer maxExecutionCount;
    private BigDecimal amount;
    private String currency;
    private String referenceId; // Account number, biller code, etc.
    private ScheduleStatus scheduleStatus; // ACTIVE, PAUSED, COMPLETED, FAILED
    private String action;
    private String additionalData; // JSON string for additional data
    private String configValue1;
    private String configValue2;
    private String configValue3;
    private String configValue4;
    private String configValue5;
    private String configValue6;
    private String configValue7;
    private String configValue8;
    private String configValue9;
    private String configValue10;
}
