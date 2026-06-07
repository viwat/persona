package com.example.persona.profile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountServiceResponse {
    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("service_code")
    private String serviceCode;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("service_icon")
    private String serviceIcon;

    @JsonProperty("badge_text")
    private String badgeText;

    @JsonProperty("badge_color")
    private String badgeColor;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("card_last_four")
    private String cardLastFour;

    @JsonProperty("status")
    private String status;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("meta_data")
    private String metaData;
}
