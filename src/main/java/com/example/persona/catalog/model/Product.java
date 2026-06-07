package com.example.persona.catalog.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dgtl_product_catalog")
@Getter
@Setter
public class Product extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "subcategory_id", nullable = false)
    private ProductSubcategory subcategory;

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
        @AttributeOverride(name = "en", column = @Column(name = "maintenance_en")),
        @AttributeOverride(name = "km", column = @Column(name = "maintenance_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "maintenance_zh"))
    })
    private MultilingualContent maintenance;

    @Column(name = "customizable_features", length = 255)
    private String customizableFeatures;

    @Column(name = "primary_icon_url", length = 255)
    private String primaryIconUrl;

    @Column(name = "secondary_icon_url", length = 255)
    private String secondaryIconUrl;

    @Column(name = "product_code", length = 255, unique = true)
    private String productCode;

    @Column(name = "display_order")
    private Integer displayOrder;

    private Boolean maintenanceMode = false;
}
