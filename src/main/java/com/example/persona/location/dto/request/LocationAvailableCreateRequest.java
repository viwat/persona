package com.example.persona.location.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationAvailableCreateRequest {

    @NotBlank
    private String code; // Unique service code

    // Multilingual name
    @NotBlank
    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("name_km")
    private String nameKm;

    @JsonProperty("name_zh")
    private String nameZh;

    // Optional icon
    @JsonProperty("icon_url")
    private String iconUrl;
}
