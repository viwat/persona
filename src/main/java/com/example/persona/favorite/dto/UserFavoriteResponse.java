package com.example.persona.favorite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("customer_key")
    private String customerKey;

    @JsonProperty("favorite_id")
    private Long favoriteId;

    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("service_code")
    private String serviceCode;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("service_icon")
    private String serviceIcon;

    @JsonProperty("pinned")
    private Boolean pinned;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("name")
    private String name;

    @JsonProperty("secondary_name")
    private String secondaryName;

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("additional_data")
    private String additionalData;

    @JsonProperty("context")
    private String context;

    @JsonProperty("config_value1")
    private String configValue1;

    @JsonProperty("config_value2")
    private String configValue2;

    @JsonProperty("config_value3")
    private String configValue3;

    @JsonProperty("config_value4")
    private String configValue4;

    @JsonProperty("config_value5")
    private String configValue5;

    @JsonProperty("config_value6")
    private String configValue6;

    @JsonProperty("config_value7")
    private String configValue7;

    @JsonProperty("config_value8")
    private String configValue8;

    @JsonProperty("config_value9")
    private String configValue9;

    @JsonProperty("config_value10")
    private String configValue10;

    @JsonProperty("metadata")
    private String metadata;
}
