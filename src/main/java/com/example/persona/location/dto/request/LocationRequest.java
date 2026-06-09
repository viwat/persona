package com.example.persona.location.dto.request;

import com.example.persona.location.mapper.GoogleMapsUrlParser;
import com.example.persona.location.model.LocationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.hibernate.validator.constraints.URL;

public sealed interface LocationRequest
        permits LocationRequest.CreateLocationRequest,
                LocationRequest.UpdateLocationRequest,
                LocationRequest.CloseLocationRequest {

    record CreateLocationRequest(
            @NotBlank(message = "name is required")
            @Size(min = 2, max = 255, message = "name must be between 2 and 255 characters")
            String name,

            @NotNull(message = "type is required") LocationType type,

            @Valid CoordinateDto coordinate,

            @NotNull(message = "address is required") @Valid AddressDto address,

            @Valid ContactInfoDto contactInfo,

            @Valid OpeningHoursDto openingHours,

            @Size(max = 20, message = "availableServices must not exceed 20 entries")
            List<
                            @NotBlank(message = "service name must not be blank")
                            @Size(max = 100, message = "service name must not exceed 100 characters") String>
                    availableServices,

            @URL(message = "logoUrl must be a valid URL starting with http:// or https://")
            @Size(max = 1000, message = "logoUrl must not exceed 1000 characters")
            String logoUrl,

            @URL(message = "coverUrl must be a valid URL starting with http:// or https://")
            @Size(max = 1000, message = "coverUrl must not exceed 1000 characters")
            String coverUrl)
            implements LocationRequest {

        public CreateLocationRequest {
            boolean hasCoordinate = coordinate != null;
            boolean hasGoogleMapsUrl = contactInfo != null
                    && contactInfo.googleMapsUrl() != null
                    && !contactInfo.googleMapsUrl().isBlank()
                    && GoogleMapsUrlParser.parse(contactInfo.googleMapsUrl()).isPresent();
            if (!hasCoordinate && !hasGoogleMapsUrl) {
                throw new IllegalArgumentException(
                        "Either 'coordinate' or a valid 'contactInfo.googleMapsUrl' must be provided");
            }
        }
    }

    record UpdateLocationRequest(
            @Size(min = 2, max = 255, message = "name must be between 2 and 255 characters")
            String name,

            @Valid CoordinateDto coordinate,

            @Valid AddressDto address,

            @Valid ContactInfoDto contactInfo,

            @Valid OpeningHoursDto openingHours,

            @Size(max = 20, message = "availableServices must not exceed 20 entries")
            List<
                            @NotBlank(message = "service name must not be blank")
                            @Size(max = 100, message = "service name must not exceed 100 characters") String>
                    availableServices,

            @URL(message = "logoUrl must be a valid URL starting with http:// or https://")
            @Size(max = 1000, message = "logoUrl must not exceed 1000 characters")
            String logoUrl,

            @URL(message = "coverUrl must be a valid URL starting with http:// or https://")
            @Size(max = 1000, message = "coverUrl must not exceed 1000 characters")
            String coverUrl)
            implements LocationRequest {}

    record CoordinateDto(
            @NotNull(message = "latitude is required")
            @DecimalMin(value = "-90.0", message = "latitude must be between -90 and 90")
            @DecimalMax(value = "90.0", message = "latitude must be between -90 and 90")
            Double latitude,

            @NotNull(message = "longitude is required")
            @DecimalMin(value = "-180.0", message = "longitude must be between -180 and 180")
            @DecimalMax(value = "180.0", message = "longitude must be between -180 and 180")
            Double longitude) {}

    record AddressDto(
            @Size(max = 500, message = "street must not exceed 500 characters")
            String street,

            @Size(max = 100, message = "commune must not exceed 100 characters")
            String commune,

            @Size(max = 100, message = "district must not exceed 100 characters")
            String district,

            @NotBlank(message = "province is required")
            @Size(max = 100, message = "province must not exceed 100 characters")
            String province,

            @Size(max = 100, message = "country must not exceed 100 characters")
            String country) {}

    record ContactInfoDto(
            @Pattern(
                    regexp = "^\\+?[0-9][0-9\\s\\-().]{4,19}$",
                    message = "phone must be a valid phone number (e.g. +855 23 999 989)")
            String phone,

            @Email(message = "email must be a valid email address")
            @Size(max = 255, message = "email must not exceed 255 characters")
            String email,

            @URL(message = "website must be a valid URL starting with http:// or https://")
            @Size(max = 500, message = "website must not exceed 500 characters")
            String website,

            @URL(message = "googleMapsUrl must be a valid URL starting with http:// or https://")
            @Size(max = 500, message = "googleMapsUrl must not exceed 500 characters")
            String googleMapsUrl) {}

    /** Optional body for PATCH /{id}/close — all fields optional. */
    record CloseLocationRequest(
            /** If null, closure has no scheduled end. ISO-8601 instant e.g. "2026-12-31T17:00:00Z". */
            Instant closedUntil) implements LocationRequest {}

    record OpeningHoursDto(
            @Valid Map<DayOfWeek, DayScheduleDto> schedule,

            @Size(max = 500, message = "specialNotes must not exceed 500 characters")
            String specialNotes) {}

    record DayScheduleDto(
            @NotBlank(message = "openTime is required")
            @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "openTime must be in HH:mm format (e.g. 08:00)")
            String openTime,

            @NotBlank(message = "closeTime is required")
            @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "closeTime must be in HH:mm format (e.g. 17:00)")
            String closeTime) {
        public DayScheduleDto {
            if (openTime != null && closeTime != null) {
                try {
                    if (!LocalTime.parse(openTime).isBefore(LocalTime.parse(closeTime))) {
                        throw new IllegalArgumentException(
                                "openTime (" + openTime + ") must be before closeTime (" + closeTime + ")");
                    }
                } catch (DateTimeParseException ignored) {
                    // format already caught by @Pattern above
                }
            }
        }
    }
}
