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
}
