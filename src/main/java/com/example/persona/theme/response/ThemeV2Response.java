package com.example.persona.theme.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ThemeV2Response {
    private Long id;
    private String displayName;
    private String displayNameKm;
    private String displayNameZh;
    private String description;
    private String descriptionKm;
    private String descriptionZh;
    private int displayOrder;
    private String previewImageUrl;
    private String themeVersion;
    private String appVersion;
    private String badgeIconKm;
    private String badgeIconZh;
    private String metadata;
}
