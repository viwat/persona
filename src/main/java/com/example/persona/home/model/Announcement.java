package com.example.persona.home.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "dgtl_announcement")
@AllArgsConstructor
public class Announcement extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "image_url_secondary")
    private String imageUrlSecondary;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "name_en")),
        @AttributeOverride(name = "km", column = @Column(name = "name_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "name_zh"))
    })
    private MultilingualContent name;

    @Column(name = "long_text")
    private String longText;

    @Column(name = "discription")
    private String discription;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "deeplink_url")
    private String deeplinkUrl;

    @Column(name = "button_url")
    private String buttonUrl;

    @Column(name = "action_type")
    private String actionType; // DEEPLINK, URL, POPUP

    @Column(name = "action_url")
    private String actionUrl;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "button_text_en")),
        @AttributeOverride(name = "km", column = @Column(name = "button_text_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "button_text_zh"))
    })
    private MultilingualContent actionButtonText;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "variant")
    private String variant; // For A/B testing or different announcement versions

    @Column(name = "segment")
    private String segment; // For targeting specific user segments (e.g., "premium", "new-user")

    @Column(name = "display_location")
    private String displayLocation; // HOME_TOP, HOME_MIDDLE, HOME_BOTTOM

    @Column(name = "display_type")
    private String displayType; // BANNER, CARD, POPUP, STORY

    @Column(name = "display_style")
    private String displayStyle; // JSON string for custom styling

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "display_duration")
    private Integer displayDuration; // in seconds

    @Column(name = "max_impressions")
    private Integer maxImpressions;

    @Column(name = "target_segments")
    private String targetSegments; // JSON array of segment IDs

    @Column(name = "target_audience")
    private String targetAudience; // JSON object for targeting rules

    @Column(name = "target_platforms")
    private String targetPlatforms;

    @Column(name = "campaign_id")
    private String campaignId;

    @Column(name = "promotion_code")
    private String promotionCode;

    @Column(name = "marketing_tags")
    private String marketingTags;

    @Column(name = "enable_tracking")
    private Boolean enableTracking;

    @Column(name = "analytics_config")
    private String analyticsConfig; // JSON object for analytics settings

    public Announcement() {}
}
