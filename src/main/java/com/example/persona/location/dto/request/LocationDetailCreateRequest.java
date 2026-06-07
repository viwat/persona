package com.example.persona.location.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class LocationDetailCreateRequest {

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

    @JsonProperty("operating_hours")
    private List<LocationOperatingHourCreateRequest> locationOperatingHours;

    @JsonProperty("available_services")
    private List<LocationAvailableCreateRequest> locationAvailableServices;
}
