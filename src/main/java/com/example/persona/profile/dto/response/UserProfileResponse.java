package com.example.persona.profile.dto.response;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserProfileResponse {
    private String variant;
    private String customerSegment;
    private String customerSubSegment;
    private String profileDisplayUrl;
    private String name;
    private boolean newCustomer;
    private String metadata;
    private String appVersion;
    private String experienceLevel;
    private String primaryAction;
    private String primaryActionTitle;
    private String primaryActionDescription;
    private String primaryActionIcon;
    private String primaryActionUrl;

    private boolean isIDVerification;
    private boolean isKYC;
    private boolean isKYCVerified;
    private boolean useOnePin;
    private boolean useBiometric;
    private boolean useSixDigitPin;
}
