package com.example.persona.location.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.location.model.LocationAvailableService;
import com.example.persona.utils.LanguageUtils;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationAvailableServiceResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("icon_url")
    private String iconUrl;

    public static LocationAvailableServiceResponse fromEntity(LocationAvailableService service) {
        if (service == null) {
            return null;
        }

        return LocationAvailableServiceResponse.builder()
                .id(service.getId())
                .code(service.getCode())
                .name(LanguageUtils.getLocalizedText(service.getName()))
                .iconUrl(service.getIconUrl())
                .build();
    }

    public static List<LocationAvailableServiceResponse> fromEntities(List<LocationAvailableService> services) {
        if (services == null || services.isEmpty()) {
            return Collections.emptyList();
        }
        return services.stream()
                .map(LocationAvailableServiceResponse::fromEntity)
                .toList();
    }
}
