package com.example.persona.misc.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dgtl_enumeration")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Enumeration extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id", length = 255, nullable = true)
    private Long parentId;

    @Column(name = "category", length = 255, nullable = false)
    private String category;

    @Column(name = "sub_category", length = 255, nullable = false)
    private String subCategory;

    @Column(name = "code", length = 255, nullable = false)
    private String code;

    @Column(name = "value", length = 255, nullable = false)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "value_en")),
        @AttributeOverride(name = "km", column = @Column(name = "value_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "value_zh"))
    })
    private MultilingualContent value;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "description", length = 500)
    private String description;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "display_value_en")),
        @AttributeOverride(name = "km", column = @Column(name = "display_value_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "display_value_zh"))
    })
    private MultilingualContent displayValue;

    @Column(name = "display_condition", length = 255)
    private String displayCondition;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "metadata", length = 1000)
    private String metadata; // JSON string for additional properties
}
