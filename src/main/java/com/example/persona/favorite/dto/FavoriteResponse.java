package com.example.persona.favorite.dto;

import lombok.Data;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FavoriteResponse {
    private Long id;
    private String serviceType;
    private String serviceCode;
    private String displayName;
    private String description;
    private String serviceIcon;
    private String appVersion;
    private String metadata;
    private String additionalData;

    private String configValue1;

    private String configValue2;

    private String configValue3;

    private String configValue4;

    private String configValue5;

    private String configValue6;

    private String configValue7;

    private String configValue8;

    private String configValue9;

    private String configValue10;
}
