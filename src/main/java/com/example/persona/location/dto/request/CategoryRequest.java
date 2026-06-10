package com.example.persona.location.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.validator.constraints.URL;

/** Admin write models for location categories and tags (FR-03 Website Locator Management). */
public interface CategoryRequest {

    /** Create/replace a category. On update the {@code code} comes from the path and the body code is ignored. */
    record CategoryUpsertRequest(
            @NotBlank(message = "code is required") @Size(max = 100, message = "code must not exceed 100 characters")
            String code,

            @NotBlank(message = "name is required") @Size(max = 255, message = "name must not exceed 255 characters")
            String name,

            @Size(max = 255, message = "nameKhmer must not exceed 255 characters")
            String nameKhmer,

            @Size(max = 1000, message = "description must not exceed 1000 characters")
            String description,

            @Size(max = 1000, message = "descriptionKhmer must not exceed 1000 characters")
            String descriptionKhmer,

            @Size(max = 255, message = "markIcon must not exceed 255 characters")
            String markIcon,

            @URL(message = "markIconUrl must be a valid URL starting with http:// or https://")
            @Size(max = 1000, message = "markIconUrl must not exceed 1000 characters")
            String markIconUrl,

            int displayOrder,

            List<@NotBlank(message = "tag code must not be blank") String> tagCodes)
            implements CategoryRequest {}

    record TagUpsertRequest(
            @NotBlank(message = "code is required") @Size(max = 100, message = "code must not exceed 100 characters")
            String code,

            @NotBlank(message = "name is required") @Size(max = 255, message = "name must not exceed 255 characters")
            String name,

            @Size(max = 255, message = "nameKhmer must not exceed 255 characters")
            String nameKhmer)
            implements CategoryRequest {}
}
