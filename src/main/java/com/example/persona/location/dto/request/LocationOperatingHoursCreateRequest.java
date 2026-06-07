package com.example.persona.location.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationOperatingHoursCreateRequest {
    @JsonProperty("requests")
    private List<LocationOperatingHourCreateRequest> requests;
}
