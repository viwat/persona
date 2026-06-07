package com.example.persona.catalog.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductResponse {
    private Long id;
    private String code;
    private String productCode;
    private String name;
    private String description;
    private String primaryIconUrl;
    private String secondaryIconUrl;
    private Boolean maintenanceMode;
    private String maintenanceMessage;
    private Integer sort;
}
