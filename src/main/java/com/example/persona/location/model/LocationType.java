package com.example.persona.location.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A location type (FR-03). Admin-managed, multilingual, icon-bearing entity
 * that the app and website render in locator filters.
 *
 * <p>Locations reference a type by its {@link #code} (loose coupling),
 * matching how {@code Location.type} stores a code string.
 */
@Entity
@Table(
        name = "dgtl_location_type",
        uniqueConstraints = {@UniqueConstraint(name = "uk_location_type_code", columnNames = "code")})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LocationType extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Business key — e.g. {@code branch}, {@code atm_crm}, {@code agent}. */
    @Column(name = "code", length = 100, nullable = false)
    private String code;

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

    /** Icon identifier (e.g. an icon-set key the app resolves). */
    @Column(name = "mark_icon", length = 255)
    private String markIcon;

    /** Fully-qualified URL to the marker icon image. */
    @Column(name = "mark_icon_url", length = 1000)
    private String markIconUrl;

    /** Controls ordering in the type list. */
    @Column(name = "display_order")
    private int displayOrder;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "dgtl_location_type_tag",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<LocationTag> tags = new LinkedHashSet<>();
}
