package com.example.persona.location.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ContactInfo {
    String phone;
    String email;
    String website;
    String googleMapsUrl;
    /** Facebook page URL (FR-03) — also feeds the app's Share action (FR-06). */
    String facebookUrl;
}
