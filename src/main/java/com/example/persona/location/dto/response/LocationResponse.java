package com.example.persona.location.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.location.model.Location;
import com.example.persona.utils.DateTimeUtils;
import com.example.persona.utils.LanguageUtils;
import com.example.persona.utils.LocationUtils;
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
public class LocationResponse {
    @JsonProperty("location_id")
    private Long locationId;

    @JsonProperty("profile_id")
    private String profileId;

    @JsonProperty("location_name")
    private String locationName;

    @JsonProperty("location_address")
    private String locationAddress;

    @JsonProperty("location_type_id")
    private Long locationTypeId;

    @JsonProperty("location_type_name")
    private String locationTypeName;

    @JsonProperty("location_type_status")
    private String locationTypeStatus;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("secondary_image_url")
    private String secondaryImageUrl;

    @JsonProperty("status")
    private String status;

    @JsonProperty("radius")
    private String radius;

    @JsonProperty("distance")
    private String distance;

    @JsonProperty("location_operating_hours")
    private List<LocationOperatingHourResponse> locationOperatingHours;

    @JsonProperty("location_available_services")
    private List<LocationAvailableServiceResponse> locationAvailableServices;

    @JsonProperty("formatted_operating_datetime")
    private String formattedOperatingDateTime;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("modified_by")
    private String modifiedBy;

    @JsonProperty("modified_date")
    private String modifiedDate;

    public static LocationResponse fromEntity(Double requestLat, Double requestLng, Location location) {
        if (location == null || location.getLocationType() == null) {
            return null;
        }
        Double distance = 0.0;
        if (requestLat != null && requestLng != null) {
            distance = LocationUtils.haversineDistanceKm(
                    requestLat, requestLng, location.getLatitude(), location.getLongitude());
        }
        return LocationResponse.builder()
                .locationId(location.getId())
                .locationName(LanguageUtils.getLocalizedText(location.getName()))
                .locationAddress(LanguageUtils.getLocalizedText(location.getAddress()))
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .imageUrl(location.getImageUrl())
                .imageUrl(location.getImageUrl())
                .secondaryImageUrl(location.getSecondaryImageUrl())
                .locationTypeId(location.getLocationType().getId())
                .locationTypeName(LanguageUtils.getLocalizedText(
                        location.getLocationType().getName()))
                .status(location.getStatus().name())
                .distance(String.valueOf(distance))
                .createdBy(location.getCreatedBy())
                .createdDate(DateTimeUtils.toEpochMillis(location.getCreatedDate()))
                .modifiedBy(location.getModifiedBy())
                .modifiedDate(DateTimeUtils.toEpochMillis(location.getModifiedDate()))
                .formattedOperatingDateTime(LocationUtils.formatOperatingDateTime(location.getOperatingHours()))
                .locationOperatingHours(LocationOperatingHourResponse.fromEntities(location.getOperatingHours()))
                .locationAvailableServices(
                        LocationAvailableServiceResponse.fromEntities(location.getAvailableServices()))
                .build();
    }

    public static List<LocationResponse> fromEntities(Double requestLat, Double requestLng, List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }
        return locations.stream()
                .map(location -> fromEntity(requestLat, requestLng, location))
                .toList();
    }
}
