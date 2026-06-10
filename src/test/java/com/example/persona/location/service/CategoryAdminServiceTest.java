package com.example.persona.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.persona.enums.StatusType;
import com.example.persona.location.dto.request.CategoryRequest.CategoryUpsertRequest;
import com.example.persona.location.dto.request.CategoryRequest.TagUpsertRequest;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryAdminService")
class CategoryAdminServiceTest {

    @Mock
    private LocationCategoryRepository categoryRepository;

    @Mock
    private LocationTagRepository tagRepository;

    private CategoryAdminService service;

    @BeforeEach
    void setUp() {
        service = new CategoryAdminService(categoryRepository, tagRepository, new LocationCategoryMapper());
    }

    private static CategoryUpsertRequest categoryRequest(List<String> tagCodes) {
        return new CategoryUpsertRequest(
                "branch", "Bank Branch", "សាខា", "desc", "desc-km", "icon", "https://cdn/i.png", 1, tagCodes);
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("persists with actor audit, ACTIVE status, and resolved tags")
        void createsCategory() {
            when(categoryRepository.existsByCode("branch")).thenReturn(false);
            when(tagRepository.findByCode("wifi"))
                    .thenReturn(Optional.of(LocationTag.builder()
                            .code("wifi")
                            .name(MultilingualContent.builder().en("WiFi").build())
                            .build()));
            when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.createCategory(categoryRequest(List.of("wifi")), "admin-1");

            ArgumentCaptor<LocationCategory> captor = ArgumentCaptor.forClass(LocationCategory.class);
            verify(categoryRepository).save(captor.capture());
            LocationCategory saved = captor.getValue();
            assertThat(saved.getCode()).isEqualTo("branch");
            assertThat(saved.getStatus()).isEqualTo(StatusType.ACTIVE);
            assertThat(saved.getCreatedBy()).isEqualTo("admin-1");
            assertThat(saved.getModifiedBy()).isEqualTo("admin-1");
            assertThat(saved.getName().getEn()).isEqualTo("Bank Branch");
            assertThat(saved.getName().getKm()).isEqualTo("សាខា");
            assertThat(saved.getTags()).extracting(LocationTag::getCode).containsExactly("wifi");
        }

        @Test
        @DisplayName("rejects a duplicate code")
        void rejectsDuplicate() {
            when(categoryRepository.existsByCode("branch")).thenReturn(true);
            assertThatThrownBy(() -> service.createCategory(categoryRequest(List.of()), "a"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("fails when a referenced tag does not exist")
        void failsOnUnknownTag() {
            when(categoryRepository.existsByCode("branch")).thenReturn(false);
            when(tagRepository.findByCode("ghost")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.createCategory(categoryRequest(List.of("ghost")), "a"))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("Tag not found: ghost");
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateCategory / deleteCategory")
    class UpdateDelete {

        @Test
        @DisplayName("update throws when category is missing")
        void updateMissing() {
            when(categoryRepository.findByCode("nope")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.updateCategory("nope", categoryRequest(List.of()), "a"))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("delete soft-deletes (status DELETED) and records the actor")
        void softDeletes() {
            LocationCategory existing = LocationCategory.builder()
                    .code("branch")
                    .status(StatusType.ACTIVE)
                    .build();
            when(categoryRepository.findByCode("branch")).thenReturn(Optional.of(existing));
            when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.deleteCategory("branch", "admin-9");

            assertThat(existing.getStatus()).isEqualTo(StatusType.DELETED);
            assertThat(existing.getModifiedBy()).isEqualTo("admin-9");
            verify(categoryRepository).save(existing);
        }
    }

    @Nested
    @DisplayName("tags")
    class Tags {

        @Test
        @DisplayName("createTag rejects duplicates")
        void createTagDuplicate() {
            when(tagRepository.existsByCode("wifi")).thenReturn(true);
            assertThatThrownBy(() -> service.createTag(new TagUpsertRequest("wifi", "WiFi", null), "a"))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(tagRepository, never()).save(any());
        }

        @Test
        @DisplayName("deleteTag throws when missing")
        void deleteTagMissing() {
            when(tagRepository.findByCode("nope")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.deleteTag("nope", "a")).isInstanceOf(CategoryNotFoundException.class);
            verify(tagRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteTag removes an existing tag")
        void deleteTag() {
            LocationTag tag = LocationTag.builder().code("wifi").build();
            when(tagRepository.findByCode("wifi")).thenReturn(Optional.of(tag));
            service.deleteTag("wifi", "admin");
            verify(tagRepository).delete(tag);
        }
    }
}
