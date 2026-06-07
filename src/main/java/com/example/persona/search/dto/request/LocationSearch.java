package com.example.persona.search.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.location.dto.response.LocationAvailableServiceResponse;
import com.example.persona.location.dto.response.LocationOperatingHourResponse;
import com.example.persona.location.dto.response.LocationResponse;
import com.example.persona.location.model.Location;
import com.example.persona.utils.DateTimeUtils;
import com.example.persona.utils.LanguageUtils;
import com.example.persona.utils.LocationUtils;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSearch implements Serializable {
    public String _id;

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

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("deep_link")
    private String deepLink;

    @JsonProperty("_geo")
    private GeoLocation geo;

    public static LocationSearch fromEntity(Location location) {
        if (location == null || location.getLocationType() == null) {
            return null;
        }
        GeoLocation geo = new GeoLocation();
        geo.setLatitude(location.getLatitude());
        geo.setLongitude(location.getLongitude());
        return LocationSearch.builder()
                .locationId(location.getId())
                .locationName(LanguageUtils.getLocalizedText(location.getName()))
                .locationAddress(LanguageUtils.getLocalizedText(location.getAddress()))
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .imageUrl(location.getImageUrl())
                .secondaryImageUrl(location.getSecondaryImageUrl())
                .locationTypeId(location.getLocationType().getId())
                .locationTypeName(LanguageUtils.getLocalizedText(
                        location.getLocationType().getName()))
                .locationTypeStatus(location.getLocationType().getStatus().name())
                .status(location.getStatus().name())
                .distance("0.0")
                .createdBy(location.getCreatedBy())
                .createdDate(DateTimeUtils.toEpochMillis(location.getCreatedDate()))
                .modifiedBy(location.getModifiedBy())
                .modifiedDate(DateTimeUtils.toEpochMillis(location.getModifiedDate()))
                .formattedOperatingDateTime(LocationUtils.formatOperatingDateTime(location.getOperatingHours()))
                .locationOperatingHours(LocationOperatingHourResponse.fromEntities(location.getOperatingHours()))
                .locationAvailableServices(
                        LocationAvailableServiceResponse.fromEntities(location.getAvailableServices()))
                .tags(LanguageUtils.getLocalizedText(location.getLocationType().getName()))
                .geo(geo)
                .build();
    }

    public static LocationSearch fromResponse(LocationResponse location) {
        if (location == null) {
            return null;
        }
        GeoLocation geo = new GeoLocation();
        geo.setLatitude(location.getLatitude());
        geo.setLongitude(location.getLongitude());
        return LocationSearch.builder()
                .locationId(location.getLocationId())
                .locationName(location.getLocationName())
                .locationAddress(location.getLocationAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .imageUrl(location.getImageUrl())
                .secondaryImageUrl(location.getSecondaryImageUrl())
                .locationTypeId(location.getLocationTypeId())
                .locationTypeName(location.getLocationTypeName())
                .status(location.getStatus())
                .distance("0.0")
                .locationTypeStatus(location.getLocationTypeStatus())
                .createdBy(location.getCreatedBy())
                .createdDate(location.getCreatedDate())
                .modifiedBy(location.getModifiedBy())
                .modifiedDate(location.getModifiedDate())
                .formattedOperatingDateTime(location.getFormattedOperatingDateTime())
                .locationOperatingHours(location.getLocationOperatingHours())
                .locationAvailableServices(location.getLocationAvailableServices())
                .tags(location.getLocationTypeName())
                .geo(geo)
                .build();
    }
}
