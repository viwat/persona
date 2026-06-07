package com.example.persona.session.dto;

import com.example.persona.session.model.UserSession;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private Long id;
    private String customerKey;
    private String customerAppId;
    private String deviceId;
    private String appVersion;
    private String osVersion;
    private String osType;
    private String deviceModel;
    private String deviceBrand;
    private String deviceManufacturer;
    private String ipAddress;
    private LocalDateTime lastLoginTime;
    private String sourceEvent;
    private String sessionSha;
    private boolean sessionChanged;

    public static SessionResponse fromEntity(UserSession session, boolean sessionChanged) {
        return SessionResponse.builder()
                .id(session.getId())
                .customerKey(session.getCustomerKey())
                .customerAppId(session.getCustomerAppId())
                .deviceId(session.getDeviceId())
                .appVersion(session.getAppVersion())
                .osVersion(session.getOsVersion())
                .osType(session.getOsType())
                .deviceModel(session.getDeviceModel())
                .deviceBrand(session.getDeviceBrand())
                .deviceManufacturer(session.getDeviceManufacturer())
                .ipAddress(session.getIpAddress())
                .lastLoginTime(session.getLastLoginTime())
                .sourceEvent(session.getSourceEvent())
                .sessionSha(session.getSessionSha())
                .sessionChanged(sessionChanged)
                .build();
    }
}
