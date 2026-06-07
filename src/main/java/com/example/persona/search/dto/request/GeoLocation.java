package com.example.persona.search.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoLocation {
    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lng")
    private double longitude;
}
