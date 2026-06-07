package com.example.persona.theme.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThemeDownloadResponse {
    private ThemeMetadata themeMetadata;
    private ThemePackageData lightMode;
    private ThemePackageData darkMode;

    @Data
    @Builder
    public static class ThemeMetadata {
        private String code;
        private String layout;
        private String displayName;
        private String description;
        private String variant;
        private String themeType;
        private String themeVersion;
        private String appVersion;
        private String previewImageUrl;
        private String animated;
        private String metadata;
    }

    @Data
    @Builder
    public static class ThemePackageData {
        private String backgroundType;
        private String backgroundUrl;
        private String textColor;
        private String textFontStyle;
        private String secondaryColor;
        private String secondaryBackgroundUrl;
        private String secondaryBackgroundType;
        private MascotData mascot;
        private List<IconData> icons;
        private String metadata;
    }

    @Data
    @Builder
    public static class MascotData {
        private String imageUrl;
        private String secondaryImageUrl;
        private String location;
        private String size;
        private String color;
        private String style;
        private AnimationData animation;
    }

    @Data
    @Builder
    public static class AnimationData {
        private String type;
        private String speed;
        private String direction;
        private String repeat;
    }

    @Data
    @Builder
    public static class IconData {
        private String code;
        private String iconUrl;
        private String iconType;
        private String iconCategory;
        private String metadata;
    }
}
