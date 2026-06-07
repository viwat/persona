package com.example.persona.search.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServiceSearch {
    public String _id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("name_km")
    private String nameKm;

    @JsonProperty("name_zh")
    private String nameZh;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("deep_link")
    private String deepLink;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("service_category")
    private String serviceCategory;

    @JsonProperty("service_sub_category")
    private String serviceSubCategory;
}
