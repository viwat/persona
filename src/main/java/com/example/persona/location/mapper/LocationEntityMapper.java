package com.example.persona.location.mapper;

import com.example.persona.location.entity.LocationEntity;
import com.example.persona.location.model.Address;
import com.example.persona.location.model.ContactInfo;
import com.example.persona.location.model.Coordinate;
import com.example.persona.location.model.Location;
import com.example.persona.location.model.LocationStatus;
import com.example.persona.location.model.LocationType;
import com.example.persona.location.model.OpeningHours;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class LocationEntityMapper {

    // ── Domain → Entity ──────────────────────────────────────────────────────

    public LocationEntity toEntity(Location domain) {
        return LocationEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .type(domain.getType().getCode())
                .status(domain.getStatus().name())
                .latitude(domain.getCoordinate().getLatitude())
                .longitude(domain.getCoordinate().getLongitude())
                .street(domain.getAddress().getStreet())
                .commune(domain.getAddress().getCommune())
                .district(domain.getAddress().getDistrict())
                .province(domain.getAddress().getProvince())
                .country(
                        domain.getAddress().getCountry() != null
                                ? domain.getAddress().getCountry()
                                : "Cambodia")
                .phone(domain.getContactInfo() != null ? domain.getContactInfo().getPhone() : null)
                .email(domain.getContactInfo() != null ? domain.getContactInfo().getEmail() : null)
                .website(
                        domain.getContactInfo() != null
                                ? domain.getContactInfo().getWebsite()
                                : null)
                .googleMapsUrl(
                        domain.getContactInfo() != null
                                ? domain.getContactInfo().getGoogleMapsUrl()
                                : null)
                .logoUrl(domain.getLogoUrl())
                .coverUrl(domain.getCoverUrl())
                .temporarilyClosed(domain.isTemporarilyClosed())
                .closedUntil(domain.getClosedUntil())
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .openingHours(toOpeningHoursJson(domain.getOpeningHours()))
                .availableServices(domain.getAvailableServices())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    // ── Entity → Domain ──────────────────────────────────────────────────────

    public Location toDomain(LocationEntity entity) {
        return Location.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(LocationType.fromCode(entity.getType()))
                .status(LocationStatus.valueOf(entity.getStatus()))
                .coordinate(new Coordinate(entity.getLatitude(), entity.getLongitude()))
                .address(Address.builder()
                        .street(entity.getStreet())
                        .commune(entity.getCommune())
                        .district(entity.getDistrict())
                        .province(entity.getProvince())
                        .country(entity.getCountry())
                        .build())
                .contactInfo(ContactInfo.builder()
                        .phone(entity.getPhone())
                        .email(entity.getEmail())
                        .website(entity.getWebsite())
                        .googleMapsUrl(entity.getGoogleMapsUrl())
                        .build())
                .logoUrl(entity.getLogoUrl())
                .coverUrl(entity.getCoverUrl())
                .temporarilyClosed(entity.isTemporarilyClosed())
                .closedUntil(entity.getClosedUntil())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .openingHours(fromOpeningHoursJson(entity.getOpeningHours()))
                .availableServices(
                        entity.getAvailableServices() != null
                                ? Collections.unmodifiableList(entity.getAvailableServices())
                                : Collections.emptyList())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Update all mutable fields of an already-managed JPA entity from a domain object.
     * ID, createdAt, and @Version are intentionally left untouched so JPA can
     * handle optimistic locking correctly without creating a second entity instance.
     * Use this in the persistence adapter's save() path for existing records to avoid
     * the NonUniqueObjectException that arises when toEntity() creates a new instance
     * with version=null while the same entity is already in the L1 cache.
     */
    public void updateEntity(LocationEntity entity, Location domain) {
        entity.setName(domain.getName());
        entity.setType(domain.getType().getCode());
        entity.setStatus(domain.getStatus().name());
        entity.setLatitude(domain.getCoordinate().getLatitude());
        entity.setLongitude(domain.getCoordinate().getLongitude());
        entity.setStreet(domain.getAddress().getStreet());
        entity.setCommune(domain.getAddress().getCommune());
        entity.setDistrict(domain.getAddress().getDistrict());
        entity.setProvince(domain.getAddress().getProvince());
        entity.setCountry(
                domain.getAddress().getCountry() != null ? domain.getAddress().getCountry() : "Cambodia");
        entity.setPhone(
                domain.getContactInfo() != null ? domain.getContactInfo().getPhone() : null);
        entity.setEmail(
                domain.getContactInfo() != null ? domain.getContactInfo().getEmail() : null);
        entity.setWebsite(
                domain.getContactInfo() != null ? domain.getContactInfo().getWebsite() : null);
        entity.setGoogleMapsUrl(
                domain.getContactInfo() != null ? domain.getContactInfo().getGoogleMapsUrl() : null);
        entity.setLogoUrl(domain.getLogoUrl());
        entity.setCoverUrl(domain.getCoverUrl());
        entity.setTemporarilyClosed(domain.isTemporarilyClosed());
        entity.setClosedUntil(domain.getClosedUntil());
        entity.setUpdatedBy(domain.getUpdatedBy());
        entity.setOpeningHours(toOpeningHoursJson(domain.getOpeningHours()));
        entity.setAvailableServices(domain.getAvailableServices());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private LocationEntity.OpeningHoursJson toOpeningHoursJson(OpeningHours oh) {
        if (oh == null) return null;
        Map<String, LocationEntity.DayScheduleJson> schedule = new LinkedHashMap<>();
        oh.getSchedule()
                .forEach((day, ds) -> schedule.put(
                        day.name(),
                        new LocationEntity.DayScheduleJson(
                                ds.getOpenTime().toString(), ds.getCloseTime().toString())));
        return new LocationEntity.OpeningHoursJson(schedule, oh.getSpecialNotes());
    }

    private OpeningHours fromOpeningHoursJson(LocationEntity.OpeningHoursJson json) {
        if (json == null) return null;
        Map<DayOfWeek, OpeningHours.DaySchedule> schedule = new LinkedHashMap<>();
        if (json.getSchedule() != null) {
            json.getSchedule()
                    .forEach((dayStr, ds) -> schedule.put(
                            DayOfWeek.valueOf(dayStr),
                            OpeningHours.DaySchedule.builder()
                                    .openTime(LocalTime.parse(ds.getOpenTime()))
                                    .closeTime(LocalTime.parse(ds.getCloseTime()))
                                    .build()));
        }
        return OpeningHours.builder()
                .schedule(schedule)
                .specialNotes(json.getSpecialNotes())
                .build();
    }
}
