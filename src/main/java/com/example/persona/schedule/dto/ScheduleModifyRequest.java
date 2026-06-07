package com.example.persona.schedule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;

@Data
public class ScheduleModifyRequest {
    @JsonProperty("schedule_type")
    private String scheduleType;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("schedule_icon")
    private String serviceIcon;

    @JsonProperty("required_fields")
    private Map<String, Object> requiredFields;

    @JsonProperty("min_amount")
    private BigDecimal minAmount;

    @JsonProperty("max_amount")
    private BigDecimal maxAmount;

    @JsonProperty("max_schedule_count")
    private Integer maxScheduleCount;

    private Map<String, Object> metadata;

    @JsonProperty("config_value1")
    private String configValue1;

    @JsonProperty("config_value2")
    private String configValue2;

    @JsonProperty("config_value3")
    private String configValue3;

    @JsonProperty("config_value4")
    private String configValue4;

    @JsonProperty("config_value5")
    private String configValue5;

    @JsonProperty("config_value6")
    private String configValue6;

    @JsonProperty("config_value7")
    private String configValue7;

    @JsonProperty("config_value8")
    private String configValue8;

    @JsonProperty("config_value9")
    private String configValue9;

    @JsonProperty("config_value10")
    private String configValue10;
}
