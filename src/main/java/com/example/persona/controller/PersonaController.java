package com.example.persona.controller;

import com.example.persona.dto.ApiResponse;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.dto.PersonaChangeRequest;
import com.example.persona.dto.response.CustomerPersonaResponse;
import com.example.persona.theme.dto.request.ThemeCustomizationRequest;
import com.example.persona.theme.dto.request.UserPersonaCreateRequest;
import com.example.persona.theme.dto.response.UserPersonaResponse;
import com.example.persona.theme.service.UserPersonaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/persona")
@RequiredArgsConstructor
@Tag(name = "Personal API", description = "APIs for managing persona data")
public class PersonaController {
    private final UserPersonaService userPersonaService;

    @PostMapping("/create")
    public ResponseEntity<@NonNull ApiResponse<UserPersonaResponse>> createUserPersona(
            @Valid @RequestBody UserPersonaCreateRequest request) {
        UserPersonaResponse response = userPersonaService.createUserPersona(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/customize")
    public ResponseEntity<@NonNull ApiResponse<CustomerPersonaResponse>> customizePersona(
            @Valid @RequestBody ThemeCustomizationRequest request) {
        CustomerPersonaResponse persona = userPersonaService.customizePersona(request.getCustomerKey(), request);
        return ResponseEntity.ok(ApiResponse.success(persona));
    }

    @PostMapping("/current")
    public ResponseEntity<@NonNull ApiResponse<CustomerPersonaResponse>> getCurrentPersona(
            @RequestBody CustomerBaseRequest request) {
        CustomerPersonaResponse persona = userPersonaService.getCurrentPersona(request.getCustomerKey());
        return ResponseEntity.ok(ApiResponse.success(persona));
    }

    @PutMapping("/change")
    public ResponseEntity<@NonNull ApiResponse<CustomerPersonaResponse>> changePersona(
            @Valid @RequestBody PersonaChangeRequest request) {
        CustomerPersonaResponse updatedPersona = userPersonaService.changePersona(request);
        return ResponseEntity.ok(ApiResponse.success(updatedPersona));
    }
}
