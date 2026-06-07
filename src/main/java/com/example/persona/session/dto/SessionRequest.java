package com.example.persona.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionRequest {

    @NotBlank(message = "Customer key is required")
    @JsonProperty("customer_key")
    private String customerKey;

    @NotBlank(message = "Customer app ID is required")
    @JsonProperty("customer_app_id")
    private String customerAppId;

    @NotBlank(message = "Device ID is required")
    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("app_version")
    @NotBlank(message = "App version is required")
    private String appVersion;

    @JsonProperty("os_version")
    @NotBlank(message = "OS version is required")
    private String osVersion;

    @JsonProperty("os_type")
    @NotBlank(message = "OS type is required")
    private String osType;

    @JsonProperty("device_model")
    @NotBlank(message = "Device model is required")
    private String deviceModel;

    @JsonProperty("device_brand")
    @NotBlank(message = "Device brand is required")
    private String deviceBrand;

    @JsonProperty("device_manufacturer")
    @NotBlank(message = "Device manufacturer is required")
    private String deviceManufacturer;

    @JsonProperty("source_event")
    @NotBlank(message = "Source event is required")
    private String sourceEvent;

    @JsonProperty("metadata")
    private String metadata;
}
