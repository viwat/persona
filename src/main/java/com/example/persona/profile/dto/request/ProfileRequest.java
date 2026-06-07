package com.example.persona.profile.dto.request;

import lombok.Data;

@Data
public class ProfileRequest {
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
}
