package com.example.persona.favorite.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserFavoriteFilterRequest {
    @NotBlank(message = "CustomerKey cannot be empty")
    private String customerKey;

    private String serviceType;
    private String serviceCode;
    private Boolean isPinned;
    private String name;
    private String referenceId;
    private String context;
}
