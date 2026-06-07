package com.example.persona.favorite.model;

import com.example.persona.model.BaseCustomerEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "dgtl_user_favorite",
        indexes = {
            @Index(name = "idx_user_favorite_user", columnList = "customer_key"),
            @Index(name = "idx_user_favorite_user_service", columnList = "customer_key, service_type"),
            @Index(name = "idx_user_favorite_user_order", columnList = "customer_key, display_order"),
            @Index(name = "idx_user_favorite_pinned", columnList = "customer_key, is_pinned")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_user_favorite_unique_ref",
                    columnNames = {"customer_key", "service_type", "service_code", "reference_id"})
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavorite extends BaseCustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_key", nullable = false)
    private String customerKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "favorite_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_favorite_favorite"))
    private Favorite favorite;

    @Column(name = "favorite_id", nullable = false, insertable = false, updatable = false)
    private Long favoriteId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(name = "app_version", nullable = false)
    private String appVersion;

    @Column(name = "icon_url", nullable = false)
    private String iconUrl;

    @Column(name = "service_icon", nullable = false)
    private String serviceIcon;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "secondary_name")
    private String secondaryName;

    @Column(name = "name", nullable = false)
    private String name; // User-defined name for this favorite

    @Column(name = "reference_id", nullable = false)
    private String referenceId; // Stores the actual reference (account number, biller code, etc.)

    @Column(name = "additional_data")
    private String additionalData; // JSON string to store service-specific data

    @Column(name = "context")
    private String context;

    @Column(name = "config_value1")
    private String configValue1;

    @Column(name = "config_value2")
    private String configValue2;

    @Column(name = "config_value3")
    private String configValue3;

    @Column(name = "config_value4")
    private String configValue4;

    @Column(name = "config_value5")
    private String configValue5;

    @Column(name = "config_value6")
    private String configValue6;

    @Column(name = "config_value7")
    private String configValue7;

    @Column(name = "config_value8")
    private String configValue8;

    @Column(name = "config_value9")
    private String configValue9;

    @Column(name = "config_value10")
    private String configValue10;

    @Column(name = "metadata")
    private String metadata;
}
