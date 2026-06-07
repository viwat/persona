package com.example.persona.home.dto;

import com.example.persona.home.model.Announcement;
import com.example.persona.utils.LanguageUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AnnouncementResponse {
    private Long id;
    private String iconUrl;
    private String name;
    private String longText;
    private String discription;
    private String deeplinkUrl;
    private String buttonUrl;
    private String actionType;
    private String actionUrl;
    private String actionButtonText;
    private String displayLocation;
    private String displayType;
    private String displayStyle;

    public static AnnouncementResponse from(Announcement announcement) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .iconUrl(announcement.getIconUrl())
                .name(LanguageUtils.getLocalizedText(announcement.getName()))
                .longText(announcement.getLongText())
                .discription(announcement.getDiscription())
                .deeplinkUrl(announcement.getDeeplinkUrl())
                .buttonUrl(announcement.getButtonUrl())
                .actionType(announcement.getActionType())
                .actionUrl(announcement.getActionUrl())
                .build();
    }
}
