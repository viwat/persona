package com.example.persona.setting.model;

import com.example.persona.model.BaseCustomerEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "dgtl_user_setting_history",
        indexes = {@Index(name = "idx_user_setting_history_id", columnList = "user_setting_id")})
@Getter
@Setter
public class UserSettingHistory extends BaseCustomerEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_setting_id", nullable = false)
    private UserSetting userSetting;

    @Column(name = "setting_key", length = 100)
    private String settingKey;

    @Column(name = "old_setting_value", length = 500)
    private String oldSettingValue;

    @Column(name = "new_setting_value", length = 500)
    private String newSettingValue;

    @Column(name = "update_reason", length = 500)
    private String updateReason;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata; // JSON string for additional properties
}
