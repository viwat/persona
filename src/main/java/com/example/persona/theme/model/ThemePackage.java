package com.example.persona.theme.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dgtl_theme_package")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThemePackage extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "background_type", length = 255)
    private String backgroundType;

    @Column(name = "background_value", length = 255)
    private String backgroundUrl;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "theme_package_id")
    private List<ThemeIconSet> icons;

    @Column(name = "metadata", length = 1000)
    private String metadata;

    @Column(name = "text_color", length = 255)
    private String textColor;

    @Column(name = "text_font_style", length = 255)
    private String textFontStyle;

    @Column(name = "secondary_color", length = 255)
    private String secondaryColor;

    @Column(name = "secondary_background_url", length = 255)
    private String secondaryBackgroundUrl;

    @Column(name = "secondary_background_type", length = 255)
    private String secondaryBackgroundType;

    @Column(name = "mascot_image_url", length = 255)
    private String mascotImageUrl;

    @Column(name = "secondary_mascot_image_url", length = 255)
    private String secondaryMascotImageUrl;

    @Column(name = "mascot_location", length = 255)
    private String mascotLocation;

    @Column(name = "mascot_size", length = 255)
    private String mascotSize;

    @Column(name = "mascot_color", length = 255)
    private String mascotColor;

    @Column(name = "mascot_style", length = 255)
    private String mascotStyle;

    @Column(name = "mascot_animation", length = 255)
    private String mascotAnimation;

    @Column(name = "mascot_animation_speed", length = 255)
    private String mascotAnimationSpeed;

    @Column(name = "mascot_animation_direction", length = 255)
    private String mascotAnimationDirection;

    @Column(name = "mascot_animation_repeat", length = 255)
    private String mascotAnimationRepeat;
}
