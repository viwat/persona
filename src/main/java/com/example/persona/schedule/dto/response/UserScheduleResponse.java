package com.example.persona.schedule.dto.response;

import com.example.persona.enums.FrequencyType;
import com.example.persona.schedule.enums.ScheduleStatus;
import com.example.persona.schedule.model.UserSchedule;
import com.example.persona.utils.ObjectMapperUtils;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserScheduleResponse implements Serializable {

    private Long id;
    private Long scheduleId;
    private String scheduleName;
    private String serviceType;
    private String serviceCode;
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
    private String referenceId;
    private ScheduleStatus scheduleStatus;

    private Map<String, String> additionalData; // parse JSON string safely
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

    // Customer info
    private String customerKey;
    private String customerNo;
    private String accountNo;
    private String phoneNo;
    private String masterAccountNo;
    private String channelCode;
    private String variant;
    private String code;
    private String deviceId;

    /**
     * Maps fromEntity entity to DTO
     */
    public static UserScheduleResponse fromEntity(UserSchedule entity) {
        ObjectMapperUtils mapper = new ObjectMapperUtils();
        Map<String, String> additionalDataMap = entity.getAdditionalData() == null
                ? null
                : mapper.readValue(entity.getAdditionalData(), new TypeReference<>() {});

        return UserScheduleResponse.builder()
                .id(entity.getId())
                .scheduleId(entity.getScheduleId())
                .scheduleName(entity.getScheduleName())
                .serviceType(entity.getServiceType())
                .serviceCode(entity.getServiceCode())
                .scheduleType(entity.getScheduleType())
                .frequency(entity.getFrequency())
                .frequencyDetail(entity.getFrequencyDetail())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .nextExecutionDateTime(entity.getNextExecutionDateTime())
                .lastExecutionDateTime(entity.getLastExecutionDateTime())
                .executionCount(entity.getExecutionCount())
                .maxExecutionCount(entity.getMaxExecutionCount())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .referenceId(entity.getReferenceId())
                .scheduleStatus(entity.getScheduleStatus())
                .additionalData(additionalDataMap)
                .configValue1(entity.getConfigValue1())
                .configValue2(entity.getConfigValue2())
                .configValue3(entity.getConfigValue3())
                .configValue4(entity.getConfigValue4())
                .configValue5(entity.getConfigValue5())
                .configValue6(entity.getConfigValue6())
                .configValue7(entity.getConfigValue7())
                .configValue8(entity.getConfigValue8())
                .configValue9(entity.getConfigValue9())
                .configValue10(entity.getConfigValue10())
                .customerKey(entity.getCustomerKey())
                .customerNo(entity.getCustomerNo())
                .accountNo(entity.getAccountNo())
                .phoneNo(entity.getPhoneNo())
                .masterAccountNo(entity.getMasterAccountNo())
                .channelCode(entity.getChannelCode())
                .variant(entity.getVariant())
                .code(entity.getCode())
                .deviceId(entity.getDeviceId())
                .build();
    }
}
