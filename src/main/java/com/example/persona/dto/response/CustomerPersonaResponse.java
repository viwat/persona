package com.example.persona.dto.response;

import lombok.Builder;
import lombok.Getter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CustomerPersonaResponse {
    private String themeCode;
    private String accentColor;
    private String textSize;
    private String appearance;
}
