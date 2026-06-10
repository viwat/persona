package com.example.persona.migration.dto;

import com.example.persona.migration.enums.MigrationStageStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStageRequest {

    @NotBlank(message = "Customer key is required")
    @JsonProperty("customer_key")
    private String customerKey;

    @NotBlank(message = "Stage code is required")
    @JsonProperty("stage_code")
    private String stageCode;

    @NotNull(message = "Stage status is required")
    @JsonProperty("stage_status")
    private MigrationStageStatus stageStatus;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("metadata")
    private String metadata;
}
