package com.example.persona.location.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.location.model.LocationType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationTypeResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    @NotBlank
    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("name_km")
    private String nameKm;

    @JsonProperty("name_zh")
    private String nameZh;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("default_image_url")
    private String defaultImageUrl;

    @JsonProperty("description")
    private String description;

    public static LocationTypeResponse fromEntity(LocationType type) {
        if (type == null) {
            return null;
        }

        return LocationTypeResponse.builder()
                .id(type.getId())
                .code(type.getCode())
                .nameEn(type.getName().getEn())
                .nameKm(type.getName().getKm())
                .nameZh(type.getName().getZh())
                .description(type.getDescription())
                .iconUrl(type.getIconUrl())
                .defaultImageUrl(type.getDefaultImageUrl())
                .build();
    }
}
