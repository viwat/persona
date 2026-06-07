package com.example.persona.controller;

import com.example.persona.dto.ApiResponse;
import com.example.persona.setting.dto.request.SettingCreateRequest;
import com.example.persona.setting.dto.request.SettingModifyRequest;
import com.example.persona.setting.dto.request.UserSettingCreateRequest;
import com.example.persona.setting.dto.response.SettingResponse;
import com.example.persona.setting.dto.response.UserSettingHistoryResponse;
import com.example.persona.setting.dto.response.UserSettingResponse;
import com.example.persona.setting.service.SettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Settings", description = "APIs for managing user settings")
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Setting API", description = "APIs for managing setting data")
public class SettingController {
    private final SettingService settingService;

    @PostMapping("/create")
    public ResponseEntity<@NonNull ApiResponse<SettingResponse>> createSetting(
            @RequestBody SettingCreateRequest request) {
        SettingResponse response = settingService.createSetting(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/batch")
    public ResponseEntity<@NonNull ApiResponse<List<SettingResponse>>> createSettings(
            @RequestBody List<SettingCreateRequest> requests) {
        List<SettingResponse> responses = settingService.createSettings(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responses));
    }

    @GetMapping("/{settingKey}/default")
    public ResponseEntity<@NonNull ApiResponse<String>> getSettingDefaultValue(@PathVariable String settingKey) {
        String value = settingService.getSettingDefaultValue(settingKey);
        return ResponseEntity.ok(ApiResponse.success(value));
    }

    @GetMapping("/{settingKey}/find")
    public ResponseEntity<@NonNull ApiResponse<SettingResponse>> getSettingBySettingKey(
            @PathVariable String settingKey) {
        SettingResponse response = settingService.getSettingBySettingKey(settingKey);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{settingKey}/delete")
    public ResponseEntity<@NonNull ApiResponse<SettingResponse>> deleteSettingBySettingKey(
            @PathVariable String settingKey) {
        SettingResponse response = settingService.deleteSetting(settingKey);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/list")
    public ResponseEntity<@NonNull ApiResponse<List<SettingResponse>>> getListSettings() {
        List<SettingResponse> responses = settingService.getListSettings();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/user-settings/create")
    public ResponseEntity<@NonNull ApiResponse<UserSettingResponse>> createUserSetting(
            @RequestBody UserSettingCreateRequest request) {
        UserSettingResponse response = settingService.createUserSetting(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/user-settings/{userSettingId}/modify")
    public ResponseEntity<@NonNull ApiResponse<UserSettingResponse>> modifyUserSetting(
            @PathVariable Long userSettingId, @RequestBody SettingModifyRequest request) {
        UserSettingResponse response = settingService.modifyUserSetting(userSettingId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user-settings/list")
    public ResponseEntity<@NonNull ApiResponse<List<UserSettingResponse>>> getUserSettingValues(
            @RequestParam String customerKey) {
        List<UserSettingResponse> responses = settingService.getUserSettingValues(customerKey);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/user-settings/{userSettingId}/values")
    public ResponseEntity<@NonNull ApiResponse<UserSettingResponse>> getUserSettingValue(
            @PathVariable Long userSettingId, @RequestParam String customerKey) {
        UserSettingResponse response = settingService.getUserSettingValue(userSettingId, customerKey);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user-setting-histories/list")
    public ResponseEntity<@NonNull ApiResponse<List<UserSettingHistoryResponse>>> getUserSettingHistories(
            @RequestParam String customerKey) {
        List<UserSettingHistoryResponse> responses = settingService.getUserSettingHistories(customerKey);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
