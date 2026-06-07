package com.example.persona.theme.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dgtl_theme")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Theme extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ThemeCategory category;

    @Column(name = "code", length = 255)
    private String code;

    @Builder.Default
    @Column(name = "layout", length = 255)
    private String layout = "4x4";

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "display_name_en")),
        @AttributeOverride(name = "km", column = @Column(name = "display_name_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "display_name_zh"))
    })
    private MultilingualContent displayName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "description_en")),
        @AttributeOverride(name = "km", column = @Column(name = "description_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "description_zh"))
    })
    private MultilingualContent description;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "customer_segment", length = 255)
    private String customerSegment;

    @Column(name = "customer_sub_segment", length = 255)
    private String customerSubSegment;

    @Column(name = "variant", length = 255)
    private String variant;

    @Column(name = "theme_type", length = 255)
    private String themeType;

    @Column(name = "theme_version", length = 255)
    private String themeVersion;

    @Column(name = "app_version", length = 255)
    private String appVersion;

    @Column(name = "badge_icon", length = 255)
    private String badgeIconKm;

    @Column(name = "badge_icon_zh", length = 255)
    private String badgeIconZh;

    @Column(name = "preview_image_url", length = 255)
    private String previewImageUrl;

    @Column(name = "animated", length = 255)
    private String animated;

    @Column(name = "metadata", length = 1000)
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "light_mode_theme_id")
    private ThemePackage lightModeThemeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dark_mode_theme_id")
    private ThemePackage darkModeThemeId;
}
