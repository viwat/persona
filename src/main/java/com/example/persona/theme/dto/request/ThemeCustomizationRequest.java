package com.example.persona.theme.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.dto.CustomerBaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeCustomizationRequest extends CustomerBaseRequest {

    @NotNull(message = "Theme ID must not be null")
    @JsonProperty("theme_id")
    private Long themeId;

    @NotNull(message = "Theme code must not be null")
    @JsonProperty("theme_code")
    private String themeCode;

    @JsonProperty("accent_color")
    @NotNull(message = "Accent color must not be null")
    private String accentColor;

    @JsonProperty("appearance")
    @NotNull(message = "Appearance must not be null")
    private String appearance; // LIGHT, DARK, SYSTEM

    @JsonProperty("text_size")
    @NotNull(message = "Text size must not be null")
    private String textSize; // SMALL, MEDIUM, LARGE
}
