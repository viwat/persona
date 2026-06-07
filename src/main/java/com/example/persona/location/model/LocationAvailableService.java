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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "dgtl_location_available_service",
        indexes = {@Index(name = "idx_service_location", columnList = "location_id")})
public class LocationAvailableService extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 255, unique = true, nullable = false)
    private String code;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "name_en")),
        @AttributeOverride(name = "km", column = @Column(name = "name_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "name_zh"))
    })
    private MultilingualContent name;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    // Each service belongs to one location
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
}
