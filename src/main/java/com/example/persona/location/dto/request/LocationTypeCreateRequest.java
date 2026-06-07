package com.example.persona.location.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationTypeCreateRequest {
    @NotBlank
    private String code;

    // Multilingual name
    @NotNull
    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("name_km")
    private String nameKm;

    @JsonProperty("name_zh")
    private String nameZh;

    @JsonProperty("description")
    private String description;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("default_image_url")
    private String defaultImageUrl;
}
