package com.example.persona.location.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LocationOperatingHourModifyRequest {
    @NotNull(message = "Day of week is required")
    @JsonProperty("day_of_week")
    private DayOfWeek dayOfWeek;

    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Time must be in HH:mm format")
    @JsonProperty("open_time")
    private String openTime;

    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Time must be in HH:mm format")
    @JsonProperty("close_time")
    private String closeTime;

    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Time must be in HH:mm format")
    @JsonProperty("second_open_time")
    private String secondOpenTime;

    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Time must be in HH:mm format")
    @JsonProperty("second_close_time")
    private String secondCloseTime;

    @NotNull(message = "Closed status is required")
    @JsonProperty("closed")
    private Boolean closed = false;
}
