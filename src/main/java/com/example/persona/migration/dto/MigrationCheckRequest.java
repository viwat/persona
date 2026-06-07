package com.example.persona.migration.dto;

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
public class MigrationCheckRequest {

    @NotBlank(message = "Customer key is required")
    @JsonProperty("customer_key")
    private String customerKey;
}
