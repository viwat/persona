package com.example.persona.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.persona.enums.StatusType;
import com.example.persona.location.dto.request.LocationTypeRequest.LocationTypeUpsertRequest;
import com.example.persona.location.dto.request.LocationTypeRequest.TagUpsertRequest;
import com.example.persona.location.exception.LocationTypeNotFoundException;
import com.example.persona.location.mapper.LocationTypeMapper;
import com.example.persona.location.model.LocationTag;
import com.example.persona.location.model.LocationType;
import com.example.persona.location.repository.LocationTagRepository;
import com.example.persona.location.repository.LocationTypeRepository;
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
@DisplayName("LocationTypeAdminService")
class LocationTypeAdminServiceTest {

    @Mock
    private LocationTypeRepository locationTypeRepository;

    @Mock
    private LocationTagRepository tagRepository;

    private LocationTypeAdminService service;

    @BeforeEach
    void setUp() {
        service = new LocationTypeAdminService(locationTypeRepository, tagRepository, new LocationTypeMapper());
    }

    private static LocationTypeUpsertRequest upsertRequest(List<String> tagCodes) {
        return new LocationTypeUpsertRequest(
                "branch", "Bank Branch", "សាខា", "desc", "desc-km", "icon", "https://cdn/i.png", 1, tagCodes);
    }

    @Nested
    @DisplayName("createLocationType")
    class CreateLocationType {

        @Test
        @DisplayName("persists with actor audit, ACTIVE status, and resolved tags")
        void createsLocationType() {
            when(locationTypeRepository.existsByCode("branch")).thenReturn(false);
            when(tagRepository.findByCode("wifi"))
                    .thenReturn(Optional.of(LocationTag.builder()
                            .code("wifi")
                            .name(MultilingualContent.builder().en("WiFi").build())
                            .build()));
            when(locationTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.createLocationType(upsertRequest(List.of("wifi")), "admin-1");

            ArgumentCaptor<LocationType> captor = ArgumentCaptor.forClass(LocationType.class);
            verify(locationTypeRepository).save(captor.capture());
            LocationType saved = captor.getValue();
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
            when(locationTypeRepository.existsByCode("branch")).thenReturn(true);
            assertThatThrownBy(() -> service.createLocationType(upsertRequest(List.of()), "a"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
            verify(locationTypeRepository, never()).save(any());
        }

        @Test
        @DisplayName("fails when a referenced tag does not exist")
        void failsOnUnknownTag() {
            when(locationTypeRepository.existsByCode("branch")).thenReturn(false);
            when(tagRepository.findByCode("ghost")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.createLocationType(upsertRequest(List.of("ghost")), "a"))
                    .isInstanceOf(LocationTypeNotFoundException.class)
                    .hasMessageContaining("Tag not found: ghost");
            verify(locationTypeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateLocationType / deleteLocationType")
    class UpdateDelete {

        @Test
        @DisplayName("update throws when location type is missing")
        void updateMissing() {
            when(locationTypeRepository.findByCode("nope")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.updateLocationType("nope", upsertRequest(List.of()), "a"))
                    .isInstanceOf(LocationTypeNotFoundException.class);
        }

        @Test
        @DisplayName("delete soft-deletes (status DELETED) and records the actor")
        void softDeletes() {
            LocationType existing = LocationType.builder()
                    .code("branch")
                    .status(StatusType.ACTIVE)
                    .build();
            when(locationTypeRepository.findByCode("branch")).thenReturn(Optional.of(existing));
            when(locationTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.deleteLocationType("branch", "admin-9");

            assertThat(existing.getStatus()).isEqualTo(StatusType.DELETED);
            assertThat(existing.getModifiedBy()).isEqualTo("admin-9");
            verify(locationTypeRepository).save(existing);
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
            assertThatThrownBy(() -> service.deleteTag("nope", "a"))
                    .isInstanceOf(LocationTypeNotFoundException.class);
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
