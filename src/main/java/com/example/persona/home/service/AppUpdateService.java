package com.example.persona.home.service;

import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.home.dto.UpdateBannerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUpdateService {

    @Value("${app.version:1.0.0}")
    private String currentAppVersion;

    @Value("${app.latest-version:1.0.0}")
    private String latestAppVersion;

    @Value("${app.force-update:false}")
    private Boolean forceUpdate;

    @Value("${app.update-url:}")
    private String updateUrl;

    public UpdateBannerResponse checkUpdate(CustomerBaseRequest request) {
        log.debug("Checking app update for customer: {}", request.getCustomerNo());

        // Compare versions
        boolean updateAvailable = isUpdateAvailable(currentAppVersion, latestAppVersion);

        if (!updateAvailable) {
            return UpdateBannerResponse.builder()
                    .updateAvailable(false)
                    .currentVersion(currentAppVersion)
                    .latestVersion(latestAppVersion)
                    .forceUpdate(false)
                    .build();
        }

        String updateMessage = String.format("Update is Available %s. Update Now", latestAppVersion);

        return UpdateBannerResponse.builder()
                .updateAvailable(true)
                .currentVersion(currentAppVersion)
                .latestVersion(latestAppVersion)
                .forceUpdate(forceUpdate)
                .updateMessage(updateMessage)
                .updateUrl(updateUrl)
                .build();
    }

    private boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        if (currentVersion == null || latestVersion == null) {
            return false;
        }

        // Simple version comparison (can be enhanced with proper semantic versioning)
        try {
            String[] currentParts = currentVersion.split("\\.");
            String[] latestParts = latestVersion.split("\\.");

            for (int i = 0; i < Math.max(currentParts.length, latestParts.length); i++) {
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

                if (latestPart > currentPart) {
                    return true;
                } else if (latestPart < currentPart) {
                    return false;
                }
            }
            return false; // Versions are equal
        } catch (NumberFormatException e) {
            log.warn("Error comparing versions: {} vs {}", currentVersion, latestVersion, e);
            return false;
        }
    }
}
