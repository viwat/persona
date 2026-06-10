package com.example.persona.home.dto;

import com.example.persona.theme.dto.response.ThemeDownloadResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomeScreenConfigResponse {
    private ThemeConfig theme;
    private GreetingResponse greeting;
    private UpdateBannerResponse updateBanner;
    private List<ServiceConfigResponse> services;
    private NavigationConfigResponse navigation;
    private QuickAccessConfigResponse quickAccess;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThemeConfig {
        private String themeCode;
        private ThemeDownloadResponse.ThemePackageData lightMode;
        private ThemeDownloadResponse.ThemePackageData darkMode;
        private String accentColor;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceConfigResponse {
        private String code;
        private String label;
        private String iconUrl;
        private String iconType;
        private Integer displayOrder;
        private Boolean enabled;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NavigationConfigResponse {
        private List<NavigationItem> items;
        private String activeColor;

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NavigationItem {
            private String code;
            private String label;
            private String iconUrl;
            private Boolean isActive;
            private Integer order;
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickAccessConfigResponse {
        private List<QuickAccessItem> items;

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class QuickAccessItem {
            private String code;
            private String label;
            private String iconUrl;
            private Integer order;
            private Boolean enabled;
        }
    }
}
