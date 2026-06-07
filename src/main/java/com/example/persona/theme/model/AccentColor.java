package com.example.persona.theme.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dgtl_accent_color")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccentColor extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "display_name_en")),
        @AttributeOverride(name = "km", column = @Column(name = "display_name_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "display_name_zh"))
    })
    private MultilingualContent displayName;

    @Column(name = "color_code", length = 255)
    private String colorCode;

    @Column(name = "rgb_code", length = 255)
    private String rgbCode;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;
}
