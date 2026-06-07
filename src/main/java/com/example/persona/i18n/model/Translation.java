package com.example.persona.i18n.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dgtl_translation")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Translation extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 255)
    private String code;

    @Column(name = "app_code", length = 255)
    private String appCode;

    @Column(name = "category", length = 255)
    private String category;

    @Column(name = "sub_category", length = 255)
    private String subCategory;

    @Column(name = "type", length = 255)
    private String type;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "name_en")),
        @AttributeOverride(name = "km", column = @Column(name = "name_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "name_zh"))
    })
    private MultilingualContent name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "metadata", length = 255)
    private String metadata;
}
