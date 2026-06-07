package com.example.persona.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class ServiceOrderUpdateRequest {
    @NotEmpty(message = "Service order list cannot be empty")
    private List<String> serviceOrder;
}
