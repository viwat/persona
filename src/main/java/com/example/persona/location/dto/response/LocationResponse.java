package com.example.persona.location.dto.response;

import com.example.persona.location.model.Location;
import com.example.persona.location.model.OpeningHours;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record LocationResponse(
        UUID id,
        String name,
        String type,
        String status,
        /** Same value as {@code type} — kept for wire compatibility while clients migrate. */
        String categoryCode,
        String branchCode,
        String branchName,
        String atmSerial,
        Double avgRating,
        String logoUrl,
        String coverUrl,
        String imageUrl,
        ActionLinkResponse action,
        OperatingStatusResponse operatingStatus,
        CoordinateResponse coordinate,
        AddressResponse address,
        ContactInfoResponse contactInfo,
        OpeningHoursResponse openingHours,
        List<String> availableServices,
        String createdBy,
        String updatedBy,
        Instant createdAt,
        Instant updatedAt) {

    public static LocationResponse from(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .type(location.getType())
                .status(location.getStatus().name())
                .categoryCode(location.getType())
                .branchCode(location.getBranchCode())
                .branchName(location.getBranchName())
                .atmSerial(location.getAtmSerial())
                .avgRating(location.getAvgRating())
                .logoUrl(location.getLogoUrl())
                .coverUrl(location.getCoverUrl())
                .imageUrl(location.getImageUrl())
                .action(ActionLinkResponse.from(location))
                .operatingStatus(new OperatingStatusResponse(location.isTemporarilyClosed(), location.getClosedUntil()))
                .coordinate(CoordinateResponse.from(location))
                .address(AddressResponse.from(location))
                .contactInfo(location.getContactInfo() != null ? ContactInfoResponse.from(location) : null)
                .openingHours(
                        location.getOpeningHours() != null
                                ? OpeningHoursResponse.from(location.getOpeningHours())
                                : null)
                .availableServices(location.getAvailableServices())
                .createdBy(location.getCreatedBy())
                .updatedBy(location.getUpdatedBy())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ActionLinkResponse(String label, String url) {
        static ActionLinkResponse from(Location l) {
            return l.getAction() != null
                    ? new ActionLinkResponse(
                            l.getAction().getLabel(), l.getAction().getUrl())
                    : null;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OperatingStatusResponse(boolean temporarilyClosed, Instant closedUntil) {}

    public record CoordinateResponse(double latitude, double longitude) {
        static CoordinateResponse from(Location l) {
            return new CoordinateResponse(
                    l.getCoordinate().getLatitude(), l.getCoordinate().getLongitude());
        }
    }

    public record AddressResponse(
            String street, String commune, String district, String province, String country, String fullAddress) {
        static AddressResponse from(Location l) {
            var a = l.getAddress();
            return new AddressResponse(
                    a.getStreet(), a.getCommune(), a.getDistrict(), a.getProvince(), a.getCountry(), a.fullAddress());
        }
    }

    public record ContactInfoResponse(
            String phone, String email, String website, String googleMapsUrl, String facebookUrl) {
        static ContactInfoResponse from(Location l) {
            var c = l.getContactInfo();
            return new ContactInfoResponse(
                    c.getPhone(), c.getEmail(), c.getWebsite(), c.getGoogleMapsUrl(), c.getFacebookUrl());
        }
    }

    public record OpeningHoursResponse(Map<String, DayScheduleResponse> schedule, String specialNotes) {
        static OpeningHoursResponse from(OpeningHours oh) {
            Map<String, DayScheduleResponse> schedule = oh.getSchedule().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().name(),
                            e -> new DayScheduleResponse(
                                    e.getValue().getOpenTime().toString(),
                                    e.getValue().getCloseTime().toString())));
            return new OpeningHoursResponse(schedule, oh.getSpecialNotes());
        }
    }

    public record DayScheduleResponse(String openTime, String closeTime) {}
}
