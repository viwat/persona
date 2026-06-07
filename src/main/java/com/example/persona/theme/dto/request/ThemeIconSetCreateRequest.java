package com.example.persona.theme.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThemeIconSetCreateRequest {

    @NotNull
    private String code;

    private String iconUrl;
    private String iconType;
    private String iconCategory;
    private String metadata;
}
