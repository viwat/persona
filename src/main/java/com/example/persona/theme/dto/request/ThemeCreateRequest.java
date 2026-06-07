package com.example.persona.theme.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.model.MultilingualContent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeCreateRequest {

    @NotNull(message = "Category ID is required")
    @JsonProperty("category_id")
    private Long categoryId;

    @NotBlank(message = "Theme code is required")
    @JsonProperty("code")
    private String code;

    @Builder.Default
    @JsonProperty("layout")
    private String layout = "4x4";

    @NotNull(message = "Display name is required")
    @JsonProperty("display_name")
    private MultilingualContent displayName;

    @JsonProperty("description")
    private MultilingualContent description;

    @Builder.Default
    @JsonProperty("display_order")
    private Integer displayOrder = 0;

    @NotNull(message = "Effective date is required")
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @NotNull(message = "Expiration date is required")
    @JsonProperty("expiration_date")
    private LocalDate expirationDate;

    @JsonProperty("customer_segment")
    private String customerSegment;

    @JsonProperty("customer_sub_segment")
    private String customerSubSegment;

    @JsonProperty("variant")
    private String variant;

    @JsonProperty("theme_type")
    private String themeType;

    @JsonProperty("theme_version")
    private String themeVersion;

    @JsonProperty("app_version")
    private String appVersion;

    @JsonProperty("badge_icon_km")
    private String badgeIconKm;

    @JsonProperty("badge_icon_zh")
    private String badgeIconZh;

    @JsonProperty("preview_image_url")
    private String previewImageUrl;

    @JsonProperty("animated")
    private String animated;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("light_mode_theme_package_id")
    private Long lightModeThemePackageId;

    @JsonProperty("dark_mode_theme_package_id")
    private Long darkModeThemePackageId;
}
