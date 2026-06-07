package com.example.persona.favorite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Slf4j
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FavoriteCreateRequest {
    @NotBlank
    private String serviceType; // TRANSFER, BILL_PAYMENT, TOP_UP

    @NotBlank
    private String serviceCode;

    @NotBlank
    private String displayName;

    private String description;

    @NotBlank
    private String serviceIcon;

    private String appVersion;

    private String metadata;

    private String additionalData;

    private String context;

    @NotNull(message = "Attributes are required")
    private Map<String, String> attributes;
}
