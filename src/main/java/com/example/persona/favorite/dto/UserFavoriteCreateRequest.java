package com.example.persona.favorite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.dto.CustomerBaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserFavoriteCreateRequest extends CustomerBaseRequest {
    @NotBlank(message = "Service type is required")
    private String serviceType;

    @NotBlank(message = "Service code is required")
    private String serviceCode;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Reference ID is required")
    private String referenceId;

    private String secondaryName;

    private String iconUrl;

    private String context;

    private Integer displayOrder;

    @JsonProperty("app_version")
    private String appVersion;

    @NotNull(message = "Attributes are required")
    private Map<String, String> attributes; // Key-value pairs for UserFavoriteAttr
}
