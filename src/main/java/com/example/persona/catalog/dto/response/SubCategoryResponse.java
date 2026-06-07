package com.example.persona.catalog.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SubCategoryResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String primaryIconUrl;
    private String secondaryIconUrl;
    private String iconBadgeUrl;
    private Boolean maintenanceMode;
    private String maintenanceMessage;
    private Integer sort;
    private List<ProductResponse> products;
}
