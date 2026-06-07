package com.example.persona.theme.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThemeResponse {
    private Long id;
    private Long categoryId;
    private String code;
    private String layout;
    private String displayName;
    private String description;
    private Integer displayOrder;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String customerSegment;
    private String customerSubSegment;
    private String variant;
    private String themeType;
    private String themeVersion;
    private String appVersion;
    private String badgeIconKm;
    private String badgeIconZh;
    private String previewImageUrl;
    private String animated;
    private String metadata;
    private String status;
    private Long lightModeThemePackageId;
    private Long darkModeThemePackageId;
}
