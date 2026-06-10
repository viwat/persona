package com.example.persona.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.persona.enums.StatusType;
import com.example.persona.location.dto.request.LocationRequest;
import com.example.persona.location.dto.request.LocationRequest.CreateLocationRequest;
import com.example.persona.location.dto.request.LocationRequest.UpdateLocationRequest;
import com.example.persona.location.entity.LocationEntity;
import com.example.persona.location.mapper.LocationEntityMapper;
import com.example.persona.location.model.Address;
import com.example.persona.location.model.Coordinate;
import com.example.persona.location.model.Location;
import com.example.persona.location.model.LocationType;
import com.example.persona.location.repository.LocationCategoryRepository;
import com.example.persona.location.repository.LocationJpaRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService — category handling")
class LocationServiceTest {

    @Mock
    private LocationJpaRepository jpaRepository;

    @Mock
    private LocationCategoryRepository categoryRepository;

    @Mock
    private LocationSearchService searchService;

    @Mock
    private LocationCacheService cacheService;

    @Mock
    private AuditService auditService;

    private LocationService service;
    private final LocationEntityMapper entityMapper = new LocationEntityMapper();

    @BeforeEach
    void setUp() {
        service = new LocationService(
                jpaRepository,
                categoryRepository,
                entityMapper,
                searchService,
                cacheService,
                auditService,
                new SimpleMeterRegistry(),
                new ObjectMapper());
    }

    private static CreateLocationRequest createRequest(String categoryCode) {
        return new CreateLocationRequest(
                "Test Location",
                LocationType.BRANCH,
                new LocationRequest.CoordinateDto(11.5, 104.9),
                new LocationRequest.AddressDto(null, null, null, "Phnom Penh", "Cambodia"),
                null, // contactInfo
                null, // openingHours
                null, // availableServices
                null, // logoUrl
                null, // coverUrl
                null, // imageUrl
                null, // branchCode
                null, // branchName
                null, // atmSerial
                categoryCode,
                null, // avgRating
                null, // actionLabel
                null); // actionUrl
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("rejects an unknown/inactive category before persisting")
        void rejectsUnknownCategory() {
            when(jpaRepository.existsByNameIgnoreCase("Test Location")).thenReturn(false);
            when(categoryRepository.existsByCodeAndStatus("ghost", StatusType.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.create(createRequest("ghost"), "actor"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown or inactive category code");

            verify(jpaRepository, never()).save(any());
        }

        @Test
        @DisplayName("defaults the category code to the type code when none is given")
        void defaultsCategoryToTypeCode() {
            when(jpaRepository.existsByNameIgnoreCase("Test Location")).thenReturn(false);
            when(jpaRepository.findById(any())).thenReturn(Optional.empty());
            when(jpaRepository.save(any(LocationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            Location saved = service.create(createRequest(null), "actor");

            // BRANCH.getCode() == "branch"; round-trips through the real mapper
            assertThat(saved.getCategoryCode()).isEqualTo("branch");
            verify(searchService).index(any());
            verify(auditService).record(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("validates the category code on update")
        void validatesCategoryOnUpdate() {
            Location existing = Location.create(
                    Location.Draft.builder()
                            .name("Existing")
                            .type(LocationType.BRANCH)
                            .coordinate(new Coordinate(11.5, 104.9))
                            .address(Address.builder().province("PP").build())
                            .build(),
                    "creator");
            LocationEntity entity = entityMapper.toEntity(existing);
            when(jpaRepository.findById(existing.getId())).thenReturn(Optional.of(entity));
            when(categoryRepository.existsByCodeAndStatus("ghost", StatusType.ACTIVE))
                    .thenReturn(false);

            UpdateLocationRequest request = new UpdateLocationRequest(
                    null, null, null, null, null, null, null, null, null, null, null, null, "ghost", null, null, null);

            assertThatThrownBy(() -> service.update(existing.getId(), request, "editor"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown or inactive category code");
            verify(jpaRepository, never()).save(any());
        }
    }
}
