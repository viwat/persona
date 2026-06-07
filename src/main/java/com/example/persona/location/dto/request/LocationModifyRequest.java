package com.example.persona.location.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.enums.StatusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationModifyRequest {
    // Multilingual name
    @NotBlank
    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("name_km")
    private String nameKm;

    @JsonProperty("name_zh")
    private String nameZh;

    // Multilingual address
    @NotNull
    @JsonProperty("address_en")
    private String addressEn;

    @JsonProperty("address_km")
    private String addressKm;

    @JsonProperty("address_zh")
    private String addressZh;

    // Coordinates
    @NotNull
    @JsonProperty("latitude")
    private Double latitude;

    @NotNull
    @JsonProperty("longitude")
    private Double longitude;

    // Images
    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("secondary_image_url")
    private String secondaryImageUrl;

    @JsonProperty("status")
    private StatusType status;
}
