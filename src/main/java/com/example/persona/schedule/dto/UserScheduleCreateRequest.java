package com.example.persona.schedule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.enums.FrequencyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserScheduleCreateRequest extends CustomerBaseRequest {
    @JsonProperty("service_type")
    @NotBlank(message = "Service type is required")
    private String serviceType;

    @JsonProperty("service_code")
    @NotBlank(message = "Service code is required")
    private String serviceCode;

    @JsonProperty("schedule_name")
    @NotBlank(message = "Schedule name is required")
    private String scheduleName;

    @JsonProperty("frequency")
    @NotBlank(message = "Frequency is required")
    private FrequencyType frequency;

    @JsonProperty("frequency_detail")
    private String frequencyDetail;

    @JsonProperty("start_date")
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("start_time")
    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @JsonProperty("end_time")
    private LocalTime endTime;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @JsonProperty("reference_id")
    @NotBlank(message = "Reference ID is required")
    private String referenceId;

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

    @JsonProperty("additional_data")
    private String additionalData;
}
