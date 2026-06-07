package com.example.persona.schedule.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ScheduleResponse {
    private Long id;
    private String serviceType;
    private String serviceCode;
    private String scheduleType;
    private String displayName;
    private String description;
    private String serviceIcon;
    private Map<String, Object> requiredFields;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer maxScheduleCount;
    private Map<String, Object> metadata;
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
