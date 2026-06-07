package com.example.persona.theme.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThemePackageResponse {
    private Long id;
    private String backgroundType;
    private String backgroundUrl;
    private List<ThemeIconSetResponse> icons;
    private String metadata;
    private String textColor;
    private String textFontStyle;
    private String secondaryColor;
    private String secondaryBackgroundUrl;
    private String secondaryBackgroundType;
    private String mascotImageUrl;
    private String secondaryMascotImageUrl;
    private String mascotLocation;
    private String mascotSize;
    private String mascotColor;
    private String mascotStyle;
    private String mascotAnimation;
    private String mascotAnimationSpeed;
    private String mascotAnimationDirection;
    private String mascotAnimationRepeat;
}
