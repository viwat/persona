package com.example.persona.setting.model;

import com.example.persona.enums.SettingCategory;
import com.example.persona.enums.SettingPermission;
import com.example.persona.enums.SettingType;
import com.example.persona.model.BaseModel;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "dgtl_setting",
        uniqueConstraints = @UniqueConstraint(columnNames = "code"),
        indexes = {
            @Index(name = "idx_setting_code", columnList = "code"),
            @Index(name = "idx_setting_key", columnList = "setting_key")
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Setting extends BaseModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_type", nullable = false, length = 50)
    private SettingType settingType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;

    @Column(name = "default_value", length = 255)
    private String defaultValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private SettingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 50)
    private SettingPermission permission;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;

    @Column(name = "callback_url", length = 500)
    private String callbackUrl;
}
