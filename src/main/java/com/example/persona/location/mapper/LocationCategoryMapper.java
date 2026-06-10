package com.example.persona.location.mapper;

import com.example.persona.location.dto.response.CategoryResponse;
import com.example.persona.location.dto.response.TagResponse;
import com.example.persona.location.model.LocationCategory;
import com.example.persona.location.model.LocationTag;
import com.example.persona.model.MultilingualContent;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Maps {@link LocationCategory}/{@link LocationTag} entities to their response DTOs. */
@Component
public class LocationCategoryMapper {

    public CategoryResponse toResponse(LocationCategory category) {
        return CategoryResponse.builder()
                .code(category.getCode())
                .name(en(category.getName()))
                .nameKhmer(km(category.getName()))
                .description(en(category.getDescription()))
                .descriptionKhmer(km(category.getDescription()))
                .markIcon(category.getMarkIcon())
                .markIconUrl(category.getMarkIconUrl())
                .displayOrder(category.getDisplayOrder())
                .tags(toTagResponses(category.getTags()))
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
