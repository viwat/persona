package com.example.persona.profile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfileResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("profile_display_url")
    private String profileDisplayUrl;

    @JsonProperty("customer_segment")
    private String customerSegment;

    @JsonProperty("customer_sub_segment")
    private String customerSubSegment;

    @JsonProperty("app_version")
    private String appVersion;

    @JsonProperty("experience_level")
    private String experienceLevel;

    @JsonProperty("primary_action")
    private String primaryAction;

    @JsonProperty("primary_action_title")
    private String primaryActionTitle;

    @JsonProperty("primary_action_description")
    private String primaryActionDescription;

    @JsonProperty("primary_action_icon")
    private String primaryActionIcon;

    @JsonProperty("primary_action_url")
    private String primaryActionUrl;

    @JsonProperty("is_id_verification")
    private boolean isIDVerification;

    @JsonProperty("is_kyc")
    private boolean isKYC;

    @JsonProperty("is_kyc_verified")
    private boolean isKYCVerified;

    @JsonProperty("use_one_pin")
    private boolean useOnePin;

    @JsonProperty("use_biometric")
    private boolean useBiometric;

    @JsonProperty("use_six_digit_pin")
    private boolean useSixDigitPin;

    @JsonProperty("local_profile")
    private LocalProfileResponse localProfile;

    @JsonProperty("global_profile")
    private GlobalProfileResponse globalProfile;
}
