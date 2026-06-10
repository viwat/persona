package com.example.persona.location.document;

import com.example.persona.location.model.ContactInfo;
import com.example.persona.location.model.Location;
import com.example.persona.location.model.OpeningHours;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Meilisearch document — flat denormalized structure for optimal search performance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDocument {

    private String id; // UUID as string (Meilisearch primary key)
    private String name;
    private String type;
    private String typeDisplayName;
    private String status;

    // Address fields indexed individually for filter/facet
    private String street;
    private String commune;
    private String district;
    private String province;
    private String country;
    private String fullAddress;

    // Coordinates for geo-search — _geo is required by Meilisearch for geo filtering/sorting
    @JsonProperty("_geo")
    private GeoPoint geo;

    private double latitude;
    private double longitude;

    // Category & source-system identifiers (FR-02 branch search, category filter)
    private String categoryCode;
    private String branchCode;
    private String branchName;
    private String atmSerial;

    // Contact info
    private String phone;
    private String email;
    private String website;
    private String googleMapsUrl;

    // Opening hours summary for display and search
    private String openingHoursNotes;
    private List<String> openDays;

    // Services
    private List<String> availableServices;

    // Computed search text for boosting
    private String searchText;

    public record GeoPoint(double lat, double lng) {}

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static LocationDocument from(Location location) {
        String fullAddress = location.getAddress().fullAddress();

        ContactInfo contact = location.getContactInfo();
        String phone = contact != null ? contact.getPhone() : null;
        String email = contact != null ? contact.getEmail() : null;
        String website = contact != null ? contact.getWebsite() : null;
        String googleMapsUrl = contact != null ? contact.getGoogleMapsUrl() : null;

        OpeningHours oh = location.getOpeningHours();
        String openingHoursNotes = oh != null ? oh.getSpecialNotes() : null;
        List<String> openDays = null;
        if (oh != null && oh.getSchedule() != null && !oh.getSchedule().isEmpty()) {
            openDays = oh.getSchedule().entrySet().stream()
                    .map(e ->
                            e.getKey().name() + " " + e.getValue().getOpenTime().format(TIME_FMT) + "-"
                                    + e.getValue().getCloseTime().format(TIME_FMT))
                    .collect(Collectors.toList());
        }

        String searchText = String.join(
                " ",
                location.getName(),
                location.getAddress().getProvince(),
                location.getAddress().getDistrict() != null
                        ? location.getAddress().getDistrict()
                        : "",
                location.getAddress().getCommune() != null
                        ? location.getAddress().getCommune()
                        : "",
                location.getAddress().getStreet() != null
                        ? location.getAddress().getStreet()
                        : "",
                location.getBranchName() != null ? location.getBranchName() : "",
                location.getBranchCode() != null ? location.getBranchCode() : "",
                location.getAtmSerial() != null ? location.getAtmSerial() : "",
                phone != null ? phone : "",
                openingHoursNotes != null ? openingHoursNotes : "");

        return LocationDocument.builder()
                .id(location.getId().toString())
                .name(location.getName())
                .type(location.getType().getCode())
                .typeDisplayName(location.getType().getDisplayName())
                .status(location.getStatus().name())
                .street(location.getAddress().getStreet())
                .commune(location.getAddress().getCommune())
                .district(location.getAddress().getDistrict())
                .province(location.getAddress().getProvince())
                .country(location.getAddress().getCountry())
                .fullAddress(fullAddress)
                .geo(new GeoPoint(
                        location.getCoordinate().getLatitude(),
                        location.getCoordinate().getLongitude()))
                .latitude(location.getCoordinate().getLatitude())
                .longitude(location.getCoordinate().getLongitude())
                .categoryCode(location.getCategoryCode())
                .branchCode(location.getBranchCode())
                .branchName(location.getBranchName())
                .atmSerial(location.getAtmSerial())
                .phone(phone)
                .email(email)
                .website(website)
                .googleMapsUrl(googleMapsUrl)
                .openingHoursNotes(openingHoursNotes)
                .openDays(openDays)
                .availableServices(location.getAvailableServices())
                .searchText(searchText.trim())
                .build();
    }
}
