package com.example.persona.location.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationNearByRequest {
    @NotNull
    @JsonProperty("location_type")
    private String locationType;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("radius_km")
    private Double radiusKm = 5.0;
}
