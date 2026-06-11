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
@DisplayName("LocationService — data-driven type handling")
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

    private static CreateLocationRequest createRequest(String type, String categoryCode) {
        return new CreateLocationRequest(
                "Test Location",
                type,
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
        @DisplayName("rejects an unknown/inactive type before persisting")
        void rejectsUnknownType() {
            when(jpaRepository.existsByNameIgnoreCase("Test Location")).thenReturn(false);
            when(categoryRepository.existsByCodeAndStatus("ghost", StatusType.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.create(createRequest("ghost", null), "actor"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown or inactive location type");

            verify(jpaRepository, never()).save(any());
        }

        @Test
        @DisplayName("accepts any active category as type — no enum, no code change")
        void acceptsAdminCreatedCategory() {
            when(jpaRepository.existsByNameIgnoreCase("Test Location")).thenReturn(false);
            when(categoryRepository.existsByCodeAndStatus("wing_point", StatusType.ACTIVE))
                    .thenReturn(true);
            when(jpaRepository.findById(any())).thenReturn(Optional.empty());
            when(jpaRepository.save(any(LocationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            Location saved = service.create(createRequest("wing_point", null), "actor");

            assertThat(saved.getType()).isEqualTo("wing_point");
            verify(searchService).index(any());
            verify(auditService).record(any());
        }

        @Test
        @DisplayName("rejects a categoryCode alias that differs from type")
        void rejectsCategoryAliasMismatch() {
            when(jpaRepository.existsByNameIgnoreCase("Test Location")).thenReturn(false);
            when(categoryRepository.existsByCodeAndStatus("branch", StatusType.ACTIVE))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.create(createRequest("branch", "atm_crm"), "actor"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not match type");

            verify(jpaRepository, never()).save(any());
        }

        @Test
        @DisplayName("accepts a categoryCode alias equal to type")
        void acceptsMatchingCategoryAlias() {
            when(jpaRepository.existsByNameIgnoreCase("Test Location")).thenReturn(false);
            when(categoryRepository.existsByCodeAndStatus("branch", StatusType.ACTIVE))
                    .thenReturn(true);
            when(jpaRepository.findById(any())).thenReturn(Optional.empty());
            when(jpaRepository.save(any(LocationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            Location saved = service.create(createRequest("branch", "branch"), "actor");

            assertThat(saved.getType()).isEqualTo("branch");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("rejects a categoryCode alias that differs from the immutable type")
        void rejectsCategoryAliasMismatchOnUpdate() {
            Location existing = Location.create(
                    Location.Draft.builder()
                            .name("Existing")
                            .type("branch")
                            .coordinate(new Coordinate(11.5, 104.9))
                            .address(Address.builder().province("PP").build())
                            .build(),
                    "creator");
            LocationEntity entity = entityMapper.toEntity(existing);
            when(jpaRepository.findById(existing.getId())).thenReturn(Optional.of(entity));

            UpdateLocationRequest request = new UpdateLocationRequest(
                    null, null, null, null, null, null, null, null, null, null, null, null, "agent", null, null, null);

            assertThatThrownBy(() -> service.update(existing.getId(), request, "editor"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not match type");
            verify(jpaRepository, never()).save(any());
        }
    }
}
