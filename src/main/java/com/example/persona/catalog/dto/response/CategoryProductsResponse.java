package com.example.persona.catalog.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryProductsResponse {
    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("category_code")
    private String categoryCode;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("category_description")
    private String categoryDescription;

    @JsonProperty("category_icon_url")
    private String categoryIconUrl;

    @JsonProperty("category_badge_url")
    private String categoryBadgeUrl;

    @JsonProperty("badge_display")
    private String badgeDisplay;

    @JsonProperty("maintenance_mode")
    private Boolean maintenanceMode;

    @JsonProperty("maintenance_message")
    private String maintenanceMessage;

    @JsonProperty("sort")
    private Integer sort;

    @JsonProperty("subcategories")
    private List<SubCategoryResponse> subcategories;
}
