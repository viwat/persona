package com.example.persona.search.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private List<Object> locationOperatingHours;

    @JsonProperty("location_available_services")
    private List<Object> locationAvailableServices;

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
}
