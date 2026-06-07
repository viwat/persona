package com.example.persona.controller;

import com.example.persona.pinless.mapper.PinlessConfigMapper;
import com.example.persona.pinless.model.dto.*;
import com.example.persona.pinless.service.PinlessAuthResolverService;
import com.example.persona.pinless.service.PinlessConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pinless")
@Validated
@RequiredArgsConstructor
public class PinlessConfigController {

    private final PinlessConfigService pinlessConfigService;
    private final PinlessAuthResolverService authResolverService;
    private final PinlessConfigMapper mapper;

    @PostMapping("/config")
    public ResponseEntity<PinlessConfigResponse> upsert(@Valid @RequestBody PinlessConfigRequest request) {
        return ResponseEntity.ok(pinlessConfigService.upsert(request));
    }

    @GetMapping("/config")
    public ResponseEntity<PinlessConfigResponse> getConfig(
            @RequestParam
                    @NotBlank(message = "customerNo is required")
                    @Size(max = 50, message = "customerNo must not exceed 50 characters")
                    String customerNo) {
        var response = mapper.toResponse(pinlessConfigService.getByCustomerNo(customerNo));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/config/toggle")
    public ResponseEntity<PinlessConfigResponse> toggle(@Valid @RequestBody PinlessToggleRequest request) {
        return ResponseEntity.ok(pinlessConfigService.toggle(request));
    }

    @GetMapping("/config/history")
    public ResponseEntity<Page<PinlessConfigHistoryResponse>> getHistory(
            @RequestParam
                    @NotBlank(message = "customerNo is required")
                    @Size(max = 50, message = "customerNo must not exceed 50 characters")
                    String customerNo,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(pinlessConfigService.getHistory(customerNo, page, size));
    }

    @PostMapping("/resolve")
    public ResponseEntity<AuthResolveResponse> resolve(@Valid @RequestBody AuthResolveRequest request) {
        return ResponseEntity.ok(authResolverService.resolve(request));
    }
}
