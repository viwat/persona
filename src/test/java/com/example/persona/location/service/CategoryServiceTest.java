package com.example.persona.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.persona.enums.StatusType;
import com.example.persona.location.exception.CategoryNotFoundException;
import com.example.persona.location.mapper.LocationCategoryMapper;
import com.example.persona.location.model.LocationCategory;
import com.example.persona.location.model.LocationTag;
import com.example.persona.location.repository.LocationCategoryRepository;
import com.example.persona.location.repository.LocationTagRepository;
import com.example.persona.model.MultilingualContent;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService")
class CategoryServiceTest {

    @Mock
    private LocationCategoryRepository categoryRepository;

    @Mock
    private LocationTagRepository tagRepository;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categoryRepository, tagRepository, new LocationCategoryMapper());
    }

    private static LocationCategory category(String code, String name) {
        return LocationCategory.builder()
                .code(code)
                .name(MultilingualContent.builder().en(name).build())
                .build();
    }

    @Test
    @DisplayName("listCategories maps active categories")
    void listCategories() {
        when(categoryRepository.findAllByStatusWithTags(StatusType.ACTIVE))
                .thenReturn(List.of(category("branch", "Branch"), category("agent", "Agent")));

        var result = service.listCategories();

        assertThat(result).extracting("code").containsExactly("branch", "agent");
    }

    @Test
    @DisplayName("getByCode returns the category when found")
    void getByCodeFound() {
        when(categoryRepository.findByCode("branch")).thenReturn(Optional.of(category("branch", "Branch")));
        assertThat(service.getByCode("branch").name()).isEqualTo("Branch");
    }

    @Test
    @DisplayName("getByCode throws when missing")
    void getByCodeMissing() {
        when(categoryRepository.findByCode("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getByCode("nope"))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Category not found: nope");
    }

    @Test
    @DisplayName("listTags maps active tags")
    void listTags() {
        when(tagRepository.findByStatusOrderByCodeAsc(StatusType.ACTIVE))
                .thenReturn(List.of(LocationTag.builder()
                        .code("wifi")
                        .name(MultilingualContent.builder().en("WiFi").build())
                        .build()));

        assertThat(service.listTags()).singleElement().extracting("code").isEqualTo("wifi");
    }

    @Test
    @DisplayName("getTag throws when missing")
    void getTagMissing() {
        when(tagRepository.findByCode("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getTag("nope"))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Tag not found: nope");
    }
}
