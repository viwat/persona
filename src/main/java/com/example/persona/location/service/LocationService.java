package com.example.persona.location.service;

import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.location.dto.request.LocationAvailableCreateRequest;
import com.example.persona.location.dto.request.LocationCreateRequest;
import com.example.persona.location.dto.request.LocationDetailCreateRequest;
import com.example.persona.location.dto.request.LocationModifyRequest;
import com.example.persona.location.dto.request.LocationOperatingHourCreateRequest;
import com.example.persona.location.dto.request.LocationOperatingHourModifyRequest;
import com.example.persona.location.dto.request.LocationTypeCreateRequest;
import com.example.persona.location.dto.response.LocationAvailableServiceResponse;
import com.example.persona.location.dto.response.LocationOperatingHourResponse;
import com.example.persona.location.dto.response.LocationResponse;
import com.example.persona.location.dto.response.LocationTypeResponse;
import com.example.persona.location.model.Location;
import com.example.persona.location.model.LocationAvailableService;
import com.example.persona.location.model.LocationOperatingHour;
import com.example.persona.location.model.LocationType;
import com.example.persona.location.repository.LocationAvailableServiceRepository;
import com.example.persona.location.repository.LocationOperatingHourRepository;
import com.example.persona.location.repository.LocationRepository;
import com.example.persona.location.repository.LocationTypeRepository;
import com.example.persona.model.MultilingualContent;
import com.example.persona.search.dto.PaginatedResult;
import com.example.persona.search.dto.request.LocationSearch;
import com.example.persona.search.service.LocationSearchService;
import com.example.persona.utils.DateTimeUtils;
import com.example.persona.utils.LocationUtils;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationTypeRepository locationTypeRepository;
    private final LocationOperatingHourRepository locationOperatingHourRepository;
    private final LocationAvailableServiceRepository locationAvailableServiceRepository;

    private final LocationSearchService locationSearchService;

    @Transactional
    public LocationResponse createLocation(LocationCreateRequest request) {
        LocationType type = locationTypeRepository
                .findById(request.getLocationTypeId())
                .orElseThrow(
                        () -> new BusinessException("LocationType Id " + request.getLocationTypeId() + " not found"));
        Location location = buildLocation(request, type);
        location = locationRepository.save(location);
        locationSearchService.addLocation(LocationSearch.fromEntity(location));
        return LocationResponse.fromEntity(null, null, location);
    }

    @Transactional
    public LocationResponse modifyLocation(Long locationId, LocationModifyRequest request) {
        try {
            Location location = locationRepository
                    .findById(locationId)
                    .orElseThrow(() -> new BusinessException("Location Id " + locationId + " not found"));
            location.setName(buildMultilingualContent(request.getNameEn(), request.getNameKm(), request.getNameZh()));
            location.setAddress(
                    buildMultilingualContent(request.getAddressEn(), request.getAddressKm(), request.getAddressZh()));
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            location.setImageUrl(request.getImageUrl());
            location.setSecondaryImageUrl(request.getSecondaryImageUrl());
            location.setStatus(request.getStatus() == null ? StatusType.ACTIVE : request.getStatus());
            location = locationRepository.save(location);
            locationSearchService.modifyLocation(LocationSearch.fromEntity(location));
            return LocationResponse.fromEntity(null, null, location);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Transactional
    public LocationResponse modifyLocationStatus(Long locationId, StatusType status) {
        try {
            Location location = locationRepository
                    .findById(locationId)
                    .orElseThrow(() -> new BusinessException("Location Id " + locationId + " not found"));
            location.setStatus(status);
            location = locationRepository.save(location);
            locationSearchService.modifyLocation(LocationSearch.fromEntity(location));
            return LocationResponse.fromEntity(null, null, location);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Transactional
    public LocationResponse createLocationWithDetail(LocationDetailCreateRequest request) {
        // Fetch LocationType
        LocationType type = locationTypeRepository
                .findById(request.getLocationTypeId())
                .filter(lt -> lt.getStatus() == StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "LocationType Id " + request.getLocationTypeId() + " not found or not active"));

        // Create Location
        Location location = new Location();

        location.setName(buildMultilingualContent(request.getNameEn(), request.getNameKm(), request.getNameZh()));
        location.setAddress(
                buildMultilingualContent(request.getAddressEn(), request.getAddressKm(), request.getAddressZh()));

        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setImageUrl(request.getImageUrl());
        location.setSecondaryImageUrl(request.getSecondaryImageUrl());
        location.setLocationType(type);
        location.setStatus(StatusType.ACTIVE);
        location.setCreatedBy(request.getCreatedBy());

        // Save location first
        locationRepository.save(location);

        // Prepare Operating Hours for batch save
        List<LocationOperatingHour> hours = request.getLocationOperatingHours().stream()
                .map(hourRequest -> buildLocationOperatingHour(hourRequest, location))
                .toList();
        locationOperatingHourRepository.saveAll(hours);

        List<LocationAvailableService> services = request.getLocationAvailableServices().stream()
                .map(serviceRequest -> buildLocationAvailableService(serviceRequest, location))
                .toList();
        locationAvailableServiceRepository.saveAll(services);

        location.setOperatingHours(hours);
        location.setAvailableServices(services);

        return LocationResponse.fromEntity(null, null, location);
    }

    public LocationTypeResponse createLocationType(LocationTypeCreateRequest request) {
        LocationType type = new LocationType();

        type.setCode(request.getCode());

        MultilingualContent name = new MultilingualContent();
        name.setEn(request.getNameEn());
        name.setKm(request.getNameKm());
        name.setZh(request.getNameZh());
        type.setName(name);

        type.setDescription(request.getDescription());
        type.setIconUrl(request.getIconUrl());
        type.setDefaultImageUrl(request.getDefaultImageUrl());
        type.setStatus(StatusType.ACTIVE);

        type = locationTypeRepository.save(type);

        return LocationTypeResponse.fromEntity(type);
    }

    public LocationAvailableServiceResponse createLocationAvailable(
            Long locationId, LocationAvailableCreateRequest request) {
        try {
            Location location = locationRepository
                    .findById(locationId)
                    .filter(lt -> lt.getStatus() == StatusType.ACTIVE)
                    .orElseThrow(() -> new BusinessException("Location not found or not active"));
            LocationAvailableService service = new LocationAvailableService();
            service.setCode(request.getCode());
            MultilingualContent name = new MultilingualContent();
            name.setEn(request.getNameEn());
            name.setKm(request.getNameKm());
            name.setZh(request.getNameZh());
            service.setName(name);
            service.setIconUrl(request.getIconUrl());
            service.setLocation(location);
            service.setStatus(StatusType.ACTIVE);
            service = locationAvailableServiceRepository.save(service);
            locationSearchService.modifyLocation(LocationSearch.fromEntity(location));
            return LocationAvailableServiceResponse.fromEntity(service);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public LocationOperatingHourResponse createLocationOperatingHour(
            Long locationId, LocationOperatingHourCreateRequest request) {
        try {
            Location location = locationRepository
                    .findById(locationId)
                    .filter(lt -> lt.getStatus() == StatusType.ACTIVE)
                    .orElseThrow(() -> new BusinessException("Location not found or not active"));
            LocationOperatingHour operatingHour = buildLocationOperatingHour(request, location);
            operatingHour = locationOperatingHourRepository.save(operatingHour);
            locationSearchService.modifyLocation(LocationSearch.fromEntity(location));
            return LocationOperatingHourResponse.fromEntity(operatingHour);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public LocationOperatingHourResponse modifyLocationOperatingHour(
            Long locationId, Long operatingHourId, LocationOperatingHourModifyRequest request) {
        try {
            LocationOperatingHour operatingHour = locationOperatingHourRepository
                    .findByIdAndLocationId(operatingHourId, locationId)
                    .orElseThrow(() -> new BusinessException("OperatingHour with ID and Location ID is not found"));
            operatingHour.setDayOfWeek(request.getDayOfWeek());
            operatingHour.setOpenTime(DateTimeUtils.parse(request.getOpenTime()));
            operatingHour.setCloseTime(DateTimeUtils.parse(request.getCloseTime()));
            operatingHour.setSecondOpenTime(DateTimeUtils.parse(request.getSecondOpenTime()));
            operatingHour.setSecondCloseTime(DateTimeUtils.parse(request.getSecondCloseTime()));
            operatingHour.setClosed(request.getClosed());
            operatingHour = locationOperatingHourRepository.save(operatingHour);
            locationSearchService.modifyLocation(LocationSearch.fromEntity(operatingHour.getLocation()));
            return LocationOperatingHourResponse.fromEntity(operatingHour);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Transactional
    public LocationAvailableServiceResponse deleteLocationAvailableService(Long locationId, Long operatingHourId) {
        try {
            LocationAvailableService availableService = locationAvailableServiceRepository
                    .findByIdAndLocationId(operatingHourId, locationId)
                    .orElseThrow(() -> new BusinessException("OperatingHour with ID and Location ID is not found"));
            Location location = availableService.getLocation();
            location.getAvailableServices().remove(availableService);
            locationAvailableServiceRepository.delete(availableService);
            locationSearchService.modifyLocation(LocationSearch.fromEntity(location));
            return LocationAvailableServiceResponse.fromEntity(availableService);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Transactional
    public LocationOperatingHourResponse deleteLocationOperatingHour(Long locationId, Long operatingHourId) {
        try {
            LocationOperatingHour operatingHour = locationOperatingHourRepository
                    .findByIdAndLocationId(operatingHourId, locationId)
                    .orElseThrow(() -> new BusinessException("OperatingHour with ID and Location ID is not found"));
            Location location = operatingHour.getLocation();
            location.getOperatingHours().remove(operatingHour);
            locationOperatingHourRepository.delete(operatingHour);
            locationSearchService.modifyLocation(LocationSearch.fromEntity(location));
            return LocationOperatingHourResponse.fromEntity(operatingHour);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Transactional
    public List<LocationOperatingHourResponse> createLocationOperatingHours(
            Long locationId, List<LocationOperatingHourCreateRequest> requests) {
        try {
            List<LocationOperatingHour> operatingHours = new ArrayList<>();
            Map<Long, Location> locationCache = new HashMap<>();
            for (LocationOperatingHourCreateRequest request : requests) {
                Location location = locationCache.computeIfAbsent(locationId, id -> locationRepository
                        .findById(id)
                        .filter(lt -> lt.getStatus() == StatusType.ACTIVE)
                        .orElseThrow(
                                () -> new BusinessException("Location not found with id: " + id + " or not active")));
                LocationOperatingHour operatingHour = buildLocationOperatingHour(request, location);
                operatingHours.add(operatingHour);
            }
            operatingHours = locationOperatingHourRepository.saveAll(operatingHours);
            operatingHours.forEach(operatingHour ->
                    locationSearchService.modifyLocation(LocationSearch.fromEntity(operatingHour.getLocation())));
            return LocationOperatingHourResponse.fromEntities(operatingHours);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new BusinessException("Error database constraints: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public LocationResponse findLocationById(Long locationId) {
        LocationResponse response = locationSearchService.searchLocationById(locationId);
        if (response == null) {
            Location location = locationRepository
                    .findById(locationId)
                    .orElseThrow(() -> new BusinessException("Location not found with id: " + locationId));
            return LocationResponse.fromEntity(null, null, location);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public PaginatedResult<LocationResponse> findLocationsNearby(
            String locationType, double latitude, double longitude, double radiusKm, Pageable pageable) {
        log.info(
                "Finding nearby locations of location type {} within {}km of ({}, {})",
                locationType,
                radiusKm,
                latitude,
                longitude);
        PaginatedResult<LocationResponse> result = locationSearchService.searchLocationsNearby(
                latitude, longitude, radiusKm * 1000, locationType, pageable);
        if (result == null) {
            log.info("Fallback to query nearby locations from DB");
            double latDistance = radiusKm / LocationUtils.EARTH_RADIUS_KM * (180.0 / Math.PI);
            double lngDistance =
                    radiusKm / (LocationUtils.EARTH_RADIUS_KM * Math.cos(Math.toRadians(latitude))) * (180.0 / Math.PI);
            double minLat = latitude - latDistance;
            double maxLat = latitude + latDistance;
            double minLng = longitude - lngDistance;
            double maxLng = longitude + lngDistance;
            Page<@NonNull Location> locationPage = locationRepository.findNearbyLocations(
                    locationType,
                    latitude,
                    longitude,
                    radiusKm,
                    minLat,
                    maxLat,
                    minLng,
                    maxLng,
                    LocationUtils.EARTH_RADIUS_KM,
                    pageable);
            List<LocationResponse> responses =
                    LocationResponse.fromEntities(latitude, longitude, locationPage.getContent());
            return PaginatedResult.of(
                    locationPage.getNumber(),
                    locationPage.getSize(),
                    locationPage.getTotalElements(),
                    locationPage.getTotalPages(),
                    responses);
        }
        return result;
    }

    @Async
    @Transactional(readOnly = true)
    public void reloadMeilisearch() {
        locationSearchService.clearAllLocations();
        locationRepository.findAll().forEach(location -> {
            locationSearchService.modifyLocation(LocationSearch.fromEntity(location));
        });
    }

    @Async
    @Transactional(readOnly = true)
    public void reloadMeilisearchByLocationId(Long locationId) {
        locationRepository.findById(locationId).ifPresent(loc -> {
            locationSearchService.modifyLocation(LocationSearch.fromEntity(loc));
        });
    }

    @Async
    @Transactional(readOnly = true)
    public void reloadMeilisearchByLocationTypeId(Long locationTypeId) {
        locationRepository.findByLocationTypeId(locationTypeId).forEach(location -> {
            locationSearchService.modifyLocation(LocationSearch.fromEntity(location));
        });
    }

    private MultilingualContent buildMultilingualContent(String en, String km, String zh) {
        MultilingualContent content = new MultilingualContent();
        content.setEn(en);
        content.setKm(km);
        content.setZh(zh);
        return content;
    }

    private Location buildLocation(LocationCreateRequest request, LocationType type) {
        Location location = new Location();
        location.setName(buildMultilingualContent(request.getNameEn(), request.getNameKm(), request.getNameZh()));
        location.setAddress(
                buildMultilingualContent(request.getAddressEn(), request.getAddressKm(), request.getAddressZh()));
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setImageUrl(request.getImageUrl());
        location.setSecondaryImageUrl(request.getSecondaryImageUrl());
        location.setLocationType(type);
        location.setStatus(StatusType.ACTIVE);
        location.setCreatedBy(request.getCreatedBy());
        return location;
    }

    @NotNull
    private LocationOperatingHour buildLocationOperatingHour(
            LocationOperatingHourCreateRequest request, Location location) {
        LocationOperatingHour operatingHour = new LocationOperatingHour();
        operatingHour.setLocation(location);
        operatingHour.setDayOfWeek(request.getDayOfWeek());
        operatingHour.setOpenTime(DateTimeUtils.parse(request.getOpenTime()));
        operatingHour.setCloseTime(DateTimeUtils.parse(request.getCloseTime()));
        operatingHour.setSecondOpenTime(DateTimeUtils.parse(request.getSecondOpenTime()));
        operatingHour.setSecondCloseTime(DateTimeUtils.parse(request.getSecondCloseTime()));
        operatingHour.setClosed(request.getClosed());
        return operatingHour;
    }

    private LocationAvailableService buildLocationAvailableService(
            LocationAvailableCreateRequest request, Location location) {
        LocationAvailableService service = new LocationAvailableService();
        service.setCode(request.getCode());
        service.setName(buildMultilingualContent(request.getNameEn(), request.getNameKm(), request.getNameZh()));
        service.setIconUrl(request.getIconUrl());
        service.setLocation(location);
        service.setStatus(StatusType.ACTIVE);
        return service;
    }
}
