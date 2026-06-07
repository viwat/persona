package com.example.persona.location.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationCreateRequest {

    // Multilingual name
    @NotNull
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

    @JsonProperty("created_by")
    private String createdBy;

    // Location Type
    @NotNull
    @JsonProperty("location_type_id")
    private Long locationTypeId;
}
