package com.example.persona.catalog.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dgtl_product_category")
@Getter
@Setter
public class ProductCategory extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "product_category_id", nullable = false)
    private Long productCategoryId;

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

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "icon_badge_url_en")),
        @AttributeOverride(name = "km", column = @Column(name = "icon_badge_url_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "icon_badge_url_zh"))
    })
    private MultilingualContent iconBadgeUrl;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "badge_display_en")),
        @AttributeOverride(name = "km", column = @Column(name = "badge_display_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "badge_display_zh"))
    })
    private MultilingualContent badgeDisplay;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "maintenance_en")),
        @AttributeOverride(name = "km", column = @Column(name = "maintenance_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "maintenance_zh"))
    })
    private MultilingualContent maintenance;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "secondary_icon_url", length = 255)
    private String secondaryIconUrl;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "maintenance_mode")
    private Integer maintenanceMode;

    @Column(name = "metadata", length = 255)
    private String metadata;
}
