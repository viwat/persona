package com.example.persona.location.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import java.util.List;
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
        name = "dgtl_location",
        indexes = {
            @Index(name = "idx_location_type", columnList = "location_type_id"),
            @Index(name = "idx_location_lat_lng", columnList = "latitude, longitude")
        })
public class Location extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "name_en")),
        @AttributeOverride(name = "km", column = @Column(name = "name_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "name_zh"))
    })
    private MultilingualContent name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "address_en")),
        @AttributeOverride(name = "km", column = @Column(name = "address_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "address_zh"))
    })
    private MultilingualContent address;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LocationAvailableService> availableServices;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LocationOperatingHour> operatingHours;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "secondary_image_url")
    private String secondaryImageUrl;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    // One location has one type
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_type_id", nullable = false)
    private LocationType locationType;
}
