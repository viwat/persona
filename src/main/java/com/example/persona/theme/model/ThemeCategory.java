package com.example.persona.theme.model;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "dgtl_theme_category",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_theme_category_code_app",
                    columnNames = {"category", "code", "app_id"})
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeCategory extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "category", length = 255)
    private String category;

    @Column(name = "app_id", length = 255)
    private String appId;

    @Column(name = "code", length = 255)
    private String code;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "name_en")),
        @AttributeOverride(name = "km", column = @Column(name = "name_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "name_zh"))
    })
    private MultilingualContent name;

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

    @Column(name = "animated", length = 255)
    private String animated;

    @Column(name = "metadata", length = 1000)
    private String metadata;

    @Column(name = "light_mode_theme_id")
    private Long lightModeThemeId;

    @Column(name = "dark_mode_theme_id")
    private Long darkModeThemeId;

    @Column(name = "badge_icon", length = 255)
    private String badgeIcon;

    @Column(name = "badge_icon_km", length = 255)
    private String badgeIconKm;

    @Column(name = "badge_icon_zh", length = 255)
    private String badgeIconZh;

    @Column(name = "display_image", length = 255)
    private String displayImage;
}
