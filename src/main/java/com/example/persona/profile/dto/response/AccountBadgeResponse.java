package com.example.persona.profile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
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
public class AccountBadgeResponse {
    @JsonProperty("badge_type")
    private String badgeType;

    @JsonProperty("badge_name")
    private String badgeName;

    @JsonProperty("badge_icon")
    private String badgeIcon;

    @JsonProperty("badge_text")
    private String badgeText;

    @JsonProperty("badge_color")
    private String badgeColor;

    @JsonProperty("badge_background_color")
    private String badgeBackgroundColor;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("expiry_date")
    private LocalDateTime expiryDate;

    @JsonProperty("meta_data")
    private String metaData;
}
