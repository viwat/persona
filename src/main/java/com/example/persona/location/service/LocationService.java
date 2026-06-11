package com.example.persona.location.service;

import com.example.persona.enums.StatusType;
import com.example.persona.location.dto.request.LocationRequest;
import com.example.persona.location.entity.LocationEntity;
import com.example.persona.location.exception.LocationDuplicateException;
import com.example.persona.location.exception.LocationNotFoundException;
import com.example.persona.location.mapper.GoogleMapsUrlParser;
import com.example.persona.location.mapper.LocationEntityMapper;
import com.example.persona.location.model.ActionLink;
import com.example.persona.location.model.Address;
import com.example.persona.location.model.AuditAction;
import com.example.persona.location.model.AuditEvent;
import com.example.persona.location.model.ContactInfo;
import com.example.persona.location.model.Coordinate;
import com.example.persona.location.model.Location;
import com.example.persona.location.model.LocationStatus;
import com.example.persona.location.model.LocationType;
import com.example.persona.location.model.OpeningHours;
import com.example.persona.location.repository.LocationCategoryRepository;
import com.example.persona.location.repository.LocationJpaRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {

    private static final double KM_PER_DEGREE_LAT = 111.32;
    private static final double METERS_PER_KM = 1000.0;

    private final LocationJpaRepository jpaRepository;
    private final LocationCategoryRepository categoryRepository;
    private final LocationEntityMapper entityMapper;
    private final LocationSearchService searchService;
    private final LocationCacheService cacheService;
    private final AuditService auditService;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

    // ── Result types ─────────────────────────────────────────────────────────

    public record SearchResult(List<Location> locations, long totalHits, int page, int size) {}

    public record NearbyResult(Location location, double distanceKm) {}

    // ── Commands ─────────────────────────────────────────────────────────────

    @Timed(value = "location.command.create")
    public Location create(LocationRequest.CreateLocationRequest request, String actor) {
        log.info("Creating location: name={}, type={}, actor={}", request.name(), request.type(), actor);

        if (jpaRepository.existsByNameIgnoreCase(request.name())) {
            throw new LocationDuplicateException("Location with name '" + request.name() + "' already exists");
        }
        validateCategory(request.type(), request.categoryCode());

        Coordinate coordinate = resolveCoordinate(request.coordinate(), request.contactInfo());
        String categoryCode = request.categoryCode() != null
                ? request.categoryCode()
                : request.type().getCode();
        Location.Draft draft = Location.Draft.builder()
                .name(request.name())
                .type(request.type())
                .coordinate(coordinate)
                .address(toAddress(request.address()))
                .contactInfo(toContactInfo(request.contactInfo()))
                .openingHours(request.openingHours() != null ? toOpeningHours(request.openingHours()) : null)
                .availableServices(request.availableServices())
                .logoUrl(request.logoUrl())
                .coverUrl(request.coverUrl())
                .imageUrl(request.imageUrl())
                .branchCode(request.branchCode())
                .branchName(request.branchName())
                .atmSerial(request.atmSerial())
                .categoryCode(categoryCode)
                .avgRating(request.avgRating())
                .action(toActionLink(request.actionLabel(), request.actionUrl()))
                .build();

        Location saved = persist(Location.create(draft, actor));
        indexAndCache(saved);
        audit(saved, AuditAction.CREATED, actor);
        log.info("Location created: id={}", saved.getId());
        return saved;
    }

    @Timed(value = "location.command.update")
    public Location update(UUID id, LocationRequest.UpdateLocationRequest request, String actor) {
        log.info("Updating location: id={}, actor={}", id, actor);

        Location existing = findOrThrow(id);

        if (request.name() != null
                && !request.name().equalsIgnoreCase(existing.getName())
                && jpaRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new LocationDuplicateException("Location with name '" + request.name() + "' already exists");
        }
        validateCategory(existing.getType(), request.categoryCode());

        Coordinate coordinate = request.coordinate() != null ? toCoordinate(request.coordinate()) : null;
        Location.Draft patch = Location.Draft.builder()
                .name(request.name())
                .coordinate(coordinate)
                .address(request.address() != null ? toAddress(request.address()) : null)
                .contactInfo(request.contactInfo() != null ? toContactInfo(request.contactInfo()) : null)
                .openingHours(request.openingHours() != null ? toOpeningHours(request.openingHours()) : null)
                .availableServices(request.availableServices())
                .logoUrl(request.logoUrl())
                .coverUrl(request.coverUrl())
                .imageUrl(request.imageUrl())
                .branchCode(request.branchCode())
                .branchName(request.branchName())
                .atmSerial(request.atmSerial())
                .categoryCode(request.categoryCode())
                .avgRating(request.avgRating())
                .action(toActionLink(request.actionLabel(), request.actionUrl()))
                .build();

        Location saved = persist(existing.update(patch, actor));
        indexAndCache(saved);
        audit(saved, AuditAction.UPDATED, actor);
        log.info("Location updated: id={}", saved.getId());
        return saved;
    }

    @Timed(value = "location.command.delete")
    public void delete(UUID id, String actor) {
        log.info("Deleting location: id={}, actor={}", id, actor);
        if (!jpaRepository.existsById(id)) throw new LocationNotFoundException(id);

        String snapshot = jpaRepository
                .findById(id)
                .map(e -> toJson(entityMapper.toDomain(e)))
                .orElse(null);

        jpaRepository.deleteById(id);
        searchService.delete(id);
        cacheService.evictById(id);
        cacheService.evictAll();
        auditService.record(AuditEvent.of(id, AuditAction.DELETED, actor, snapshot));
        log.info("Location deleted: id={}", id);
    }

    @Timed(value = "location.command.activate")
    public Location activate(UUID id, String actor) {
        Location saved = persist(findOrThrow(id).activate(actor));
        indexAndCache(saved);
        audit(saved, AuditAction.ACTIVATED, actor);
        log.info("Location activated: id={}", id);
        return saved;
    }

    @Timed(value = "location.command.deactivate")
    public Location deactivate(UUID id, String actor) {
        Location saved = persist(findOrThrow(id).deactivate(actor));
        searchService.delete(id);
        cacheService.evictById(id);
        cacheService.evictAll();
        audit(saved, AuditAction.DEACTIVATED, actor);
        log.info("Location deactivated: id={}", id);
        return saved;
    }

    @Timed(value = "location.command.closeTemporarily")
    public Location closeTemporarily(UUID id, Instant closedUntil, String actor) {
        log.info("Temporarily closing location: id={}, until={}, actor={}", id, closedUntil, actor);
        Location saved = persist(findOrThrow(id).closeTemporarily(closedUntil, actor));
        indexAndCache(saved);
        audit(saved, AuditAction.CLOSED_TEMPORARILY, actor);
        return saved;
    }

    @Timed(value = "location.command.reopen")
    public Location reopen(UUID id, String actor) {
        log.info("Reopening location: id={}, actor={}", id, actor);
        Location saved = persist(findOrThrow(id).reopen(actor));
        indexAndCache(saved);
        audit(saved, AuditAction.REOPENED, actor);
        return saved;
    }

    /**
     * Patches only the logo or cover image URL, then indexes, caches, and audits.
     * Keeps image-upload concerns out of the controller and reuses the standard write path.
     */
    @Timed(value = "location.command.updateImage")
    public Location updateImage(UUID id, boolean logo, String url, String actor) {
        Location existing = findOrThrow(id);
        Location.Draft patch = Location.Draft.builder()
                .logoUrl(logo ? url : null)
                .coverUrl(logo ? null : url)
                .build();
        Location saved = persist(existing.update(patch, actor));
        indexAndCache(saved);
        auditService.record(AuditEvent.of(
                id,
                AuditAction.IMAGE_UPLOADED,
                actor,
                "{\"type\":\"" + (logo ? "logo" : "cover") + "\",\"url\":\"" + url + "\"}"));
        log.info("Image updated for location {}: type={}, url={}", id, logo ? "logo" : "cover", url);
        return saved;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Timed(value = "location.query.findById")
    public Optional<Location> findById(UUID id) {
        Optional<Location> cached = cacheService.getById(id);
        if (cached.isPresent()) {
            meterRegistry.counter("location.cache.hit", "operation", "findById").increment();
            return cached;
        }
        meterRegistry.counter("location.cache.miss", "operation", "findById").increment();
        Optional<Location> location = jpaRepository.findById(id).map(entityMapper::toDomain);
        location.ifPresent(loc -> {
            cacheService.putById(loc);
            log.debug("Location {} loaded from DB and cached", id);
        });
        return location;
    }

    @Transactional(readOnly = true)
    @Timed(value = "location.query.browse")
    public SearchResult browse(List<LocationType> types, int page, int size) {
        List<LocationType> effective =
                (types == null || types.isEmpty()) ? Arrays.asList(LocationType.values()) : types;
        List<String> codes = effective.stream().map(LocationType::getCode).collect(Collectors.toList());
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<LocationEntity> result =
                jpaRepository.findByTypeInAndStatus(codes, LocationStatus.ACTIVE.name(), pageable);
        List<Location> content =
                result.getContent().stream().map(entityMapper::toDomain).toList();
        return new SearchResult(content, result.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    @Timed(value = "location.query.findAll")
    public List<Location> findAll(List<LocationType> types) {
        List<LocationType> effective =
                (types == null || types.isEmpty()) ? Arrays.asList(LocationType.values()) : types;
        Optional<List<Location>> cached = cacheService.getAllByTypes(effective);
        if (cached.isPresent()) {
            meterRegistry.counter("location.cache.hit", "operation", "findAll").increment();
            return cached.get();
        }
        meterRegistry.counter("location.cache.miss", "operation", "findAll").increment();
        List<String> codes = effective.stream().map(LocationType::getCode).collect(Collectors.toList());
        List<Location> locations = jpaRepository.findByTypeInAndStatus(codes, LocationStatus.ACTIVE.name()).stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
        cacheService.putAllByTypes(effective, locations);
        return locations;
    }

    @Transactional(readOnly = true)
    @Timed(value = "location.query.search")
    public SearchResult search(
            String text,
            List<LocationType> types,
            String province,
            String district,
            String commune,
            int page,
            int size) {
        try {
            LocationSearchService.SearchIndexResult indexResult =
                    searchService.search(text, types, province, district, commune, page * size, size);
            if (indexResult.locationIds().isEmpty()) return new SearchResult(Collections.emptyList(), 0, page, size);
            List<Location> locations = indexResult.locationIds().stream()
                    .map(id -> findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
            meterRegistry.counter("location.search.meilisearch").increment();
            return new SearchResult(locations, indexResult.totalHits(), page, size);
        } catch (Exception e) {
            log.warn("Meilisearch unavailable, falling back to PostgreSQL: {}", e.getMessage());
            meterRegistry.counter("location.search.fallback").increment();
            return searchFallback(text, types, province, district, commune, page, size);
        }
    }

    @Transactional(readOnly = true)
    @Timed(value = "location.query.nearby")
    public List<NearbyResult> findNearby(Coordinate center, double radiusKm, List<LocationType> types, int limit) {
        try {
            List<LocationSearchService.NearbyIndexResult> indexResults =
                    searchService.findNearby(center, radiusKm, types, limit);
            List<NearbyResult> results = indexResults.stream()
                    .map(r -> findById(r.locationId())
                            .map(loc -> new NearbyResult(loc, loc.distanceTo(center)))
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
            meterRegistry.counter("location.nearby.meilisearch").increment();
            return results;
        } catch (Exception e) {
            log.warn("Meilisearch unavailable for nearby search, falling back to PostgreSQL: {}", e.getMessage());
            meterRegistry.counter("location.nearby.fallback").increment();
            return findNearbyFallback(center, radiusKm, types, limit);
        }
    }

    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    // ── Private — persistence ─────────────────────────────────────────────────

    private Location persist(Location location) {
        if (location.getId() != null) {
            Optional<LocationEntity> managed = jpaRepository.findById(location.getId());
            if (managed.isPresent()) {
                entityMapper.updateEntity(managed.get(), location);
                return entityMapper.toDomain(jpaRepository.save(managed.get()));
            }
        }
        return entityMapper.toDomain(jpaRepository.save(entityMapper.toEntity(location)));
    }

    private Location findOrThrow(UUID id) {
        return jpaRepository
                .findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> new LocationNotFoundException(id));
    }

    // ── Private — search fallback ─────────────────────────────────────────────

    private SearchResult searchFallback(
            String text,
            List<LocationType> types,
            String province,
            String district,
            String commune,
            int page,
            int size) {
        String typesArray = toPostgresArray(types);
        int offset = page * size;
        List<Location> locations = jpaRepository
                .fullTextSearch(
                        emptyToNull(text),
                        typesArray,
                        emptyToNull(province),
                        emptyToNull(district),
                        emptyToNull(commune),
                        offset,
                        size)
                .stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
        long total = jpaRepository.countFullTextSearch(
                emptyToNull(text), typesArray, emptyToNull(province), emptyToNull(district), emptyToNull(commune));
        return new SearchResult(locations, total, page, size);
    }

    private List<NearbyResult> findNearbyFallback(
            Coordinate center, double radiusKm, List<LocationType> types, int limit) {
        String typesArray = toPostgresArray(types);
        double radiusMeters = radiusKm * METERS_PER_KM;
        double deltaLat = radiusKm / KM_PER_DEGREE_LAT;
        double deltaLon = radiusKm / (KM_PER_DEGREE_LAT * Math.cos(Math.toRadians(center.getLatitude())));

        List<Object[]> rows = jpaRepository.findNearbyRaw(
                center.getLatitude(),
                center.getLongitude(),
                center.getLatitude() - deltaLat,
                center.getLatitude() + deltaLat,
                center.getLongitude() - deltaLon,
                center.getLongitude() + deltaLon,
                radiusMeters,
                typesArray,
                limit);

        if (rows.isEmpty()) return List.of();

        List<UUID> ids = rows.stream().map(row -> (UUID) row[0]).collect(Collectors.toList());
        Map<UUID, Double> distByIdMeters = rows.stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[row.length - 1]).doubleValue()));

        Map<UUID, LocationEntity> entityById =
                jpaRepository.findAllById(ids).stream().collect(Collectors.toMap(LocationEntity::getId, e -> e));

        return ids.stream()
                .map(entityById::get)
                .filter(Objects::nonNull)
                .map(e -> new NearbyResult(entityMapper.toDomain(e), distByIdMeters.get(e.getId()) / METERS_PER_KM))
                .collect(Collectors.toList());
    }

    // ── Private — side effects ────────────────────────────────────────────────

    private void indexAndCache(Location location) {
        try {
            searchService.index(location);
        } catch (Exception e) {
            log.warn("Failed to index location {} in Meilisearch: {}", location.getId(), e.getMessage());
        }
        cacheService.putById(location);
        cacheService.evictAll();
    }

    private void audit(Location location, AuditAction action, String actor) {
        auditService.record(AuditEvent.of(location.getId(), action, actor, toJson(location)));
    }

    private String toJson(Location location) {
        try {
            return objectMapper.writeValueAsString(location);
        } catch (JacksonException e) {
            log.warn("Failed to serialize location snapshot: {}", e.getMessage());
            return null;
        }
    }

    // ── Private — DTO mapping ─────────────────────────────────────────────────

    private Coordinate resolveCoordinate(
            LocationRequest.CoordinateDto dto, LocationRequest.ContactInfoDto contactInfo) {
        if (dto != null) return toCoordinate(dto);
        if (contactInfo != null && contactInfo.googleMapsUrl() != null) {
            return GoogleMapsUrlParser.parse(contactInfo.googleMapsUrl())
                    .map(ll -> new Coordinate(ll.latitude(), ll.longitude()))
                    .orElse(null);
        }
        return null;
    }

    private Coordinate toCoordinate(LocationRequest.CoordinateDto dto) {
        return new Coordinate(dto.latitude(), dto.longitude());
    }

    private Address toAddress(LocationRequest.AddressDto dto) {
        return Address.builder()
                .street(dto.street())
                .commune(dto.commune())
                .district(dto.district())
                .province(dto.province())
                .country(dto.country())
                .build();
    }

    private ContactInfo toContactInfo(LocationRequest.ContactInfoDto dto) {
        if (dto == null) return null;
        return ContactInfo.builder()
                .phone(dto.phone())
                .email(dto.email())
                .website(dto.website())
                .googleMapsUrl(dto.googleMapsUrl())
                .facebookUrl(dto.facebookUrl())
                .build();
    }

    private ActionLink toActionLink(String label, String url) {
        if (label == null && url == null) return null;
        return ActionLink.builder().label(label).url(url).build();
    }

    /**
     * Rejects an unknown or inactive category code so locations only reference live categories.
     * Also rejects a code that differs from the location type's code: category codes mirror
     * LocationType codes during the enum→category transition, and silent drift between the two
     * fields would corrupt type-based filtering. Lift this guard when categories become the
     * single source of truth (v2 API).
     */
    private void validateCategory(LocationType type, String categoryCode) {
        if (categoryCode == null) return;
        if (!categoryRepository.existsByCodeAndStatus(categoryCode, StatusType.ACTIVE)) {
            throw new IllegalArgumentException("Unknown or inactive category code: " + categoryCode);
        }
        if (!categoryCode.equals(type.getCode())) {
            throw new IllegalArgumentException("Category code '" + categoryCode
                    + "' does not match location type '" + type.getCode() + "'");
        }
    }

    private OpeningHours toOpeningHours(LocationRequest.OpeningHoursDto dto) {
        if (dto.schedule() == null)
            return OpeningHours.builder().specialNotes(dto.specialNotes()).build();
        Map<DayOfWeek, OpeningHours.DaySchedule> schedule = new LinkedHashMap<>();
        dto.schedule()
                .forEach((day, ds) -> schedule.put(
                        day,
                        OpeningHours.DaySchedule.builder()
                                .openTime(LocalTime.parse(ds.openTime()))
                                .closeTime(LocalTime.parse(ds.closeTime()))
                                .build()));
        return OpeningHours.builder()
                .schedule(schedule)
                .specialNotes(dto.specialNotes())
                .build();
    }

    private String toPostgresArray(List<LocationType> types) {
        if (types == null || types.isEmpty()) return null;
        return types.stream().map(LocationType::getCode).collect(Collectors.joining(",", "{", "}"));
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
