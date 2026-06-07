package com.example.persona.pinless.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PinlessToggleRequest {

    @NotBlank(message = "customerNo is required")
    private String customerNo;

    @NotNull(message = "enabled is required")
    private Boolean enabled;

    private String changeReason;
}
