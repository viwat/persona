package com.example.persona.home.dto;

import com.example.persona.home.model.Promotional;
import com.example.persona.utils.LanguageUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PromotionResponse {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String thumbnailUrl;
    private String videoUrl;
    private String imageUrlSecondary;
    private String deeplinkUrl;
    private String actionType;
    private String actionUrl;
    private String actionButtonText;
    private String displayLocation;
    private String displayType;
    private String displayStyle;
    private Integer displayOrder;
    private Integer displayDuration;
    private Integer maxImpressions;
    private String targetSegments;
    private String targetAudience;
    private String targetPlatforms;

    public static PromotionResponse from(Promotional promotional) {
        return PromotionResponse.builder()
                .id(promotional.getId())
                .title(LanguageUtils.getLocalizedText(promotional.getTitle()))
                .description(LanguageUtils.getLocalizedText(promotional.getDescription()))
                .imageUrl(promotional.getImageUrl())
                .thumbnailUrl(promotional.getThumbnailUrl())
                .videoUrl(promotional.getVideoUrl())
                .imageUrlSecondary(promotional.getImageUrlSecondary())
                .deeplinkUrl(promotional.getDeeplinkUrl())
                .actionType(promotional.getActionType())
                .actionUrl(promotional.getActionUrl())
                .actionButtonText(LanguageUtils.getLocalizedText(promotional.getActionButtonText()))
                .displayLocation(promotional.getDisplayLocation())
                .displayType(promotional.getDisplayType())
                .build();
    }
}
