package com.example.persona.setting.model;

import com.example.persona.model.BaseCustomerEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(
        name = "dgtl_user_setting",
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"customer_no", "account_no", "channel_code", "setting_id"}),
        indexes = {
            @Index(name = "idx_user_setting_lookup", columnList = "customer_no,account_no,channel_code,setting_id")
        })
@Getter
@Setter
public class UserSetting extends BaseCustomerEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setting_id", nullable = false)
    private Setting setting;

    @Column(name = "setting_value", length = 255)
    private String settingValue;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata; // JSON string for additional properties
}
