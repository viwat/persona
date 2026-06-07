package com.example.persona.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private String requestId;
    private String sessionId;
    private String userId;
    private String customerKey;
    private String username;
    private String osPlatform;
    private String appId;
    private String latitude;
    private String longitude;
    private String defaultAccount;
    private String segments;
    private String subSegments;
    private String region;
    private String deviceType;
    private String appVersion;
    private String osVersion;
    private String deviceId;
    private String ipAddress;
}
