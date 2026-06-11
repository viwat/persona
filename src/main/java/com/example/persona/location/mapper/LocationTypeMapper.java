package com.example.persona.location.mapper;

import com.example.persona.location.dto.response.LocationTypeResponse;
import com.example.persona.location.dto.response.TagResponse;
import com.example.persona.location.model.LocationTag;
import com.example.persona.location.model.LocationType;
import com.example.persona.model.MultilingualContent;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Maps {@link LocationType}/{@link LocationTag} entities to their response DTOs. */
@Component
public class LocationTypeMapper {

    public LocationTypeResponse toResponse(LocationType locationType) {
        return LocationTypeResponse.builder()
                .code(locationType.getCode())
                .name(en(locationType.getName()))
                .nameKhmer(km(locationType.getName()))
                .description(en(locationType.getDescription()))
                .descriptionKhmer(km(locationType.getDescription()))
                .markIcon(locationType.getMarkIcon())
                .markIconUrl(locationType.getMarkIconUrl())
                .displayOrder(locationType.getDisplayOrder())
                .tags(toTagResponses(locationType.getTags()))
                .build();
    }

    public TagResponse toTagResponse(LocationTag tag) {
        return TagResponse.builder()
                .code(tag.getCode())
                .name(en(tag.getName()))
                .nameKhmer(km(tag.getName()))
                .build();
    }

    private List<TagResponse> toTagResponses(Set<LocationTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .sorted(Comparator.comparing(LocationTag::getCode, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toTagResponse)
                .toList();
    }

    private String en(MultilingualContent content) {
        return Optional.ofNullable(content).map(MultilingualContent::getEn).orElse(null);
    }

    private String km(MultilingualContent content) {
        return Optional.ofNullable(content).map(MultilingualContent::getKm).orElse(null);
    }
}
