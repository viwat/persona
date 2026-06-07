package com.example.persona.catalog.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "dgtl_product_sub_category")
public class ProductSubcategory extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(name = "code", nullable = false)
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

    @Column(name = "primary_icon_url", length = 255)
    private String primaryIconUrl;

    @Column(name = "secondary_icon_url", length = 255)
    private String secondaryIconUrl;

    @Column(name = "service_type", length = 255)
    private String serviceType;

    private Integer sort = 1;

    private Boolean isActive = true;

    private Boolean maintenanceMode = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    protected void onModify() {
        updatedAt = LocalDateTime.now();
    }
}
