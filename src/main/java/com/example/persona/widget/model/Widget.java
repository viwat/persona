package com.example.persona.widget.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dgtl_widget")
public class Widget extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String code;

    private String customerSegment;

    private String customerSubSegment;

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

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "display_order")
    private String displayOrder;

    @Column(name = "preview_url")
    private String previewUrl;

    @Column(name = "deep_link")
    private String deepLink;

    @Column(name = "secondary_preview_url")
    private String secondaryPreviewUrl;

    @Column(name = "widget_version")
    private String widgetVersion;

    @Column(name = "params")
    private String params;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "param1_key")
    private String param1Key;

    @Column(name = "param1_type")
    private String param1Type;

    @Column(name = "param2_key")
    private String param2Key;

    @Column(name = "param2_type")
    private String param2Type;

    @Column(name = "param3_key")
    private String param3Key;

    @Column(name = "param3_type")
    private String param3Type;

    @Column(name = "param4_key")
    private String param4Key;

    @Column(name = "param4_type")
    private String param4Type;

    @Column(name = "param5_key")
    private String param5Key;

    @Column(name = "param5_type")
    private String param5Type;
}
