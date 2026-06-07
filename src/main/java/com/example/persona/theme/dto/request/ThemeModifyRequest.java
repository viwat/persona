package com.example.persona.theme.dto.request;

import com.example.persona.model.MultilingualContent;
import java.time.LocalDate;
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
public class ThemeModifyRequest {
    private Long id;
    private Long categoryId;
    private String code;
    private String layout;
    private MultilingualContent displayName;
    private MultilingualContent description;
    private int displayOrder;
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
    private Long lightModeThemeId;
    private Long darkModeThemeId;
}
