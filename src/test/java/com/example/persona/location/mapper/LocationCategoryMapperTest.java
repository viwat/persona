package com.example.persona.location.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.persona.location.dto.response.CategoryResponse;
import com.example.persona.location.dto.response.TagResponse;
import com.example.persona.location.model.LocationCategory;
import com.example.persona.location.model.LocationTag;
import com.example.persona.model.MultilingualContent;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LocationCategoryMapper")
class LocationCategoryMapperTest {

    private final LocationCategoryMapper mapper = new LocationCategoryMapper();

    private static MultilingualContent ml(String en, String km) {
        return MultilingualContent.builder().en(en).km(km).build();
    }

    private static LocationTag tag(String code, String en, String km) {
        return LocationTag.builder().code(code).name(ml(en, km)).build();
    }

    @Test
    @DisplayName("maps English/Khmer names, icons, display order")
    void mapsCategory() {
        LocationCategory category = LocationCategory.builder()
                .code("branch")
                .name(ml("Bank Branch", "សាខា"))
                .description(ml("desc-en", "desc-km"))
                .markIcon("branch-icon")
                .markIconUrl("https://cdn/icon.png")
                .displayOrder(2)
                .build();

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response.code()).isEqualTo("branch");
        assertThat(response.name()).isEqualTo("Bank Branch");
        assertThat(response.nameKhmer()).isEqualTo("សាខា");
        assertThat(response.description()).isEqualTo("desc-en");
        assertThat(response.descriptionKhmer()).isEqualTo("desc-km");
        assertThat(response.markIcon()).isEqualTo("branch-icon");
        assertThat(response.markIconUrl()).isEqualTo("https://cdn/icon.png");
        assertThat(response.displayOrder()).isEqualTo(2);
        assertThat(response.tags()).isEmpty();
    }

    @Test
    @DisplayName("returns null name parts and empty tags when absent")
    void handlesNulls() {
        LocationCategory category = LocationCategory.builder().code("agent").build();

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response.name()).isNull();
        assertThat(response.nameKhmer()).isNull();
        assertThat(response.description()).isNull();
        assertThat(response.tags()).isEmpty();
    }

    @Test
    @DisplayName("sorts tags by code for stable output")
    void sortsTags() {
        Set<LocationTag> tags = new LinkedHashSet<>();
        tags.add(tag("wifi", "WiFi", "វ៉ាយហ្វាយ"));
        tags.add(tag("atm24", "24h ATM", "ATM ២៤ម៉ោង"));
        LocationCategory category =
                LocationCategory.builder().code("branch").tags(tags).build();

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response.tags()).extracting(TagResponse::code).containsExactly("atm24", "wifi");
    }
}
