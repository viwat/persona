package com.example.persona.theme.dto.request;

import com.example.persona.enums.StatusType;
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
public class ThemeCategoryModifyRequest {
    private String category;
    private String appId;
    private String code;
    private MultilingualContent name;
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
    private String animated;
    private String metadata;
    private Long lightModeThemeId;
    private Long darkModeThemeId;
    private String badgeIcon;
    private String badgeIconKm;
    private String badgeIconZh;
    private String displayImage;
    private StatusType status;
}
