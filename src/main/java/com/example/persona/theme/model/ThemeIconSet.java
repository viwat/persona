package com.example.persona.theme.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "dgtl_theme_icon_set",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_theme_package_code",
                    columnNames = {"theme_package_id", "code"})
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeIconSet extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 255)
    private String code;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "icon_type", length = 255)
    private String iconType;

    @Column(name = "icon_category", length = 255)
    private String iconCategory;

    @Column(name = "metadata", length = 255)
    private String metadata;
}
