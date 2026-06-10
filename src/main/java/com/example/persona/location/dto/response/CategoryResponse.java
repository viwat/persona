package com.example.persona.location.dto.response;

import java.util.List;
import lombok.Builder;

/**
 * FR-03 Category projection returned by the category endpoints.
 * camelCase to stay consistent with the rest of the location module's responses.
 */
@Builder
public record CategoryResponse(
        String code,
        String name,
        String nameKhmer,
        String description,
        String descriptionKhmer,
        String markIcon,
        String markIconUrl,
        int displayOrder,
        List<TagResponse> tags) {}
