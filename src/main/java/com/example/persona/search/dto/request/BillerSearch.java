package com.example.persona.search.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillerSearch {
    public String _id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("name_km")
    private String nameKm;

    @JsonProperty("name_zh")
    private String nameZh;

    @JsonProperty("category")
    private String category;

    @JsonProperty("sub_category")
    private String subCategory;

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("deep_link")
    private String deepLink;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("location")
    private GeoLocation location;
}
