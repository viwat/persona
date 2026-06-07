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
public class SubmitMigrationRequest {

    @NotBlank(message = "Customer key is required")
    @JsonProperty("customer_key")
    private String customerKey;

    @NotBlank(message = "Master account number is required")
    @JsonProperty("master_account_no")
    private String masterAccountNo;
}
