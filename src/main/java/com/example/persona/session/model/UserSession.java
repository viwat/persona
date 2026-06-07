package com.example.persona.session.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.*;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dgtl_user_session")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_key", nullable = false)
    private String customerKey;

    @Column(name = "customer_app_id")
    private String customerAppId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "os_version", length = 100)
    private String osVersion;

    @Column(name = "os_type", length = 20)
    private String osType;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "device_brand")
    private String deviceBrand;

    @Column(name = "device_manufacturer")
    private String deviceManufacturer;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "source_event")
    private String sourceEvent;

    @Column(name = "session_sha", length = 64)
    private String sessionSha;

    @Column(name = "metadata", length = 1000)
    private String metadata;

    /**
     * Generates a SHA value based on session attributes for comparison. This is
     * used to detect changes in app version, OS version, or device.
     */
    public String generateSessionSha() {
        String content =
                String.join("|", nullSafe(appVersion), nullSafe(osVersion), nullSafe(osType), nullSafe(deviceId));
        return sha256(content);
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String sha256(String content) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
