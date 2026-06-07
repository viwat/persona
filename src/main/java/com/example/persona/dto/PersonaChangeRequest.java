package com.example.persona.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PersonaChangeRequest extends CustomerBaseRequest {
    private String themeCode;
    private String accentColor;
    private String textSize;
    private String appearance;
    private String personaCode;

    // At least one of themeCode or personaCode must be provided
    @AssertTrue(message = "Either themeCode or accentColor must be provided")
    public boolean isValidRequest() {
        return themeCode != null || accentColor != null;
    }
}
