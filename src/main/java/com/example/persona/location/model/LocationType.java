package com.example.persona.location.model;

import com.example.persona.model.BaseModel;
import com.example.persona.model.MultilingualContent;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "dgtl_location_type",
        indexes = {@Index(name = "idx_location_type_code", columnList = "code")})
public class LocationType extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "en", column = @Column(name = "name_en")),
        @AttributeOverride(name = "km", column = @Column(name = "name_km")),
        @AttributeOverride(name = "zh", column = @Column(name = "name_zh"))
    })
    private MultilingualContent name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "default_image_url")
    private String defaultImageUrl;

    // One type has many locations
    @OneToMany(mappedBy = "locationType")
    private List<Location> locations;
}
