package com.example.persona.location.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.location.model.LocationOperatingHour;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationOperatingHourResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("day_of_week")
    private String dayOfWeek;

    @JsonProperty("open_time")
    private String openTime;

    @JsonProperty("close_time")
    private String closeTime;

    @JsonProperty("second_open_time")
    private String secondOpenTime;

    @JsonProperty("second_close_time")
    private String secondCloseTime;

    @JsonProperty("closed")
    private String closed;

    public static LocationOperatingHourResponse fromEntity(LocationOperatingHour entity) {
        if (entity == null) return null;

        return LocationOperatingHourResponse.builder()
                .id(entity.getId())
                .dayOfWeek(entity.getDayOfWeek() != null ? entity.getDayOfWeek().name() : null)
                .openTime(toStr(entity.getOpenTime()))
                .closeTime(toStr(entity.getCloseTime()))
                .secondOpenTime(toStr(entity.getSecondOpenTime()))
                .secondCloseTime(toStr(entity.getSecondCloseTime()))
                .closed(String.valueOf(entity.isClosed()))
                .build();
    }

    private static String toStr(LocalTime time) {
        return time != null ? time.toString() : null; // HH:mm
    }

    public static List<LocationOperatingHourResponse> fromEntities(List<LocationOperatingHour> list) {
        if (list == null) return Collections.emptyList();

        return list.stream().map(LocationOperatingHourResponse::fromEntity).toList();
    }
}
