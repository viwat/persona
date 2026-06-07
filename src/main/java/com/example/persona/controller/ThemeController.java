package com.example.persona.controller;

import com.example.persona.config.annotation.LocalizedResponse;
import com.example.persona.dto.ApiResponse;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.exception.AppException;
import com.example.persona.home.dto.HomeScreenConfigResponse;
import com.example.persona.home.service.HomeScreenConfigService;
import com.example.persona.theme.dto.request.AccentColorCreateRequest;
import com.example.persona.theme.dto.request.ThemeCategoryCreateRequest;
import com.example.persona.theme.dto.request.ThemeCategoryModifyRequest;
import com.example.persona.theme.dto.request.ThemeCreateRequest;
import com.example.persona.theme.dto.request.ThemeIconSetCreateRequest;
import com.example.persona.theme.dto.request.ThemePackageCreateRequest;
import com.example.persona.theme.dto.request.ThemePackageModifyRequest;
import com.example.persona.theme.dto.request.ThemePreviewRequest;
import com.example.persona.theme.dto.response.AccentColorResponse;
import com.example.persona.theme.dto.response.ThemeCategoryResponse;
import com.example.persona.theme.dto.response.ThemeDownloadResponse;
import com.example.persona.theme.dto.response.ThemeIconSetResponse;
import com.example.persona.theme.dto.response.ThemePackageResponse;
import com.example.persona.theme.dto.response.ThemePreviewListResponse;
import com.example.persona.theme.dto.response.ThemeResponse;
import com.example.persona.theme.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/themes")
@AllArgsConstructor
@LocalizedResponse
@Tag(name = "Theme API", description = "APIs for managing theme data")
public class ThemeController {

    private final ThemeService themeService;
    private final HomeScreenConfigService homeScreenConfigService;

    @PostMapping("/create-icon-sets")
    public ResponseEntity<@NonNull ApiResponse<List<ThemeIconSetResponse>>> createThemeCategory(
            @Valid @RequestBody List<ThemeIconSetCreateRequest> request) {
        List<ThemeIconSetResponse> responses = themeService.createIconSets(request);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/create-package")
    public ResponseEntity<@NonNull ApiResponse<ThemePackageResponse>> createThemeCategory(
            @Valid @RequestBody ThemePackageCreateRequest request) {
        ThemePackageResponse response = themeService.createThemePackage(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/packages/{id}/find")
    public ResponseEntity<@NonNull ApiResponse<ThemePackageResponse>> findThemePackage(@PathVariable Long id) {
        ThemePackageResponse response = themeService.findThemePackageById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/packages/{id}/modify")
    public ResponseEntity<@NonNull ApiResponse<ThemePackageResponse>> modifyThemePackage(
            @PathVariable Long id, @Valid @RequestBody ThemePackageModifyRequest request) {
        ThemePackageResponse response = themeService.modifyThemePackage(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/create-category")
    public ResponseEntity<@NonNull ApiResponse<ThemeCategoryResponse>> createThemeCategory(
            @Valid @RequestBody ThemeCategoryCreateRequest request) {
        ThemeCategoryResponse response = themeService.createThemeCategory(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/categories/{id}/find")
    public ResponseEntity<@NonNull ApiResponse<ThemeCategoryResponse>> findThemeCategory(@PathVariable Long id) {
        ThemeCategoryResponse response = themeService.findThemeCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/categories/{id}/modify")
    public ResponseEntity<@NonNull ApiResponse<ThemeCategoryResponse>> modifyThemeCategory(
            @PathVariable Long id, @Valid @RequestBody ThemeCategoryModifyRequest request) {
        ThemeCategoryResponse response = themeService.modifyThemeCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/create")
    @Operation(
            summary = "Create theme",
            description = "Creates a new theme with category, display names, theme packages, and configuration")
    public ResponseEntity<@NonNull ApiResponse<ThemeResponse>> createTheme(
            @Valid @RequestBody ThemeCreateRequest request) {
        ThemeResponse response = themeService.createTheme(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/list")
    @Operation(summary = "Get all themes", description = "Get all themes")
    public ResponseEntity<@NonNull ApiResponse<List<ThemeResponse>>> getAllThemes() {
        List<ThemeResponse> responses = themeService.getAllThemes();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/preview")
    @Operation(
            summary = "Get active themes",
            description = "Gets all active themes grouped by category for the specified customer segment")
    public ResponseEntity<@NonNull ApiResponse<ThemePreviewListResponse>> getActiveThemes(
            @Valid @RequestBody ThemePreviewRequest request) {
        ThemePreviewListResponse response = themeService.getActiveThemes(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{code}/download")
    @Operation(
            summary = "Download theme",
            description = "Downloads a complete theme package with light/dark modes, colors, mascot, and icons")
    public ResponseEntity<@NonNull ApiResponse<ThemeDownloadResponse>> downloadTheme(@PathVariable String code) {
        try {
            ThemeDownloadResponse response = themeService.downloadTheme(code);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new AppException("Error retrieving theme: " + e.getMessage());
        }
    }

    @GetMapping("/{code}/install")
    public ResponseEntity<@NonNull ApiResponse<ThemeResponse>> installTheme(@PathVariable String code) {
        try {
            ThemeResponse response = themeService.installTheme(code);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new AppException("Error retrieving theme: " + e.getMessage());
        }
    }

    @PostMapping("/accent-colors/create")
    public ResponseEntity<@NonNull ApiResponse<AccentColorResponse>> createAccentColor(
            @Valid @RequestBody AccentColorCreateRequest request) {
        AccentColorResponse response = themeService.createAccentColor(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accent-colors/list")
    @Operation(summary = "Get active accent colors", description = "Returns all available accent color options")
    public ResponseEntity<@NonNull ApiResponse<List<AccentColorResponse>>> getActiveAccentColors() {
        List<AccentColorResponse> accentColors = themeService.getActiveAccentColors();
        return ResponseEntity.ok(ApiResponse.success(accentColors));
    }

    @PostMapping("/active-config")
    @Operation(
            summary = "Get active theme configuration",
            description = "Returns the current user's active theme configuration including colors, mascot, and icons")
    public ResponseEntity<@NonNull ApiResponse<HomeScreenConfigResponse.ThemeConfig>> getActiveThemeConfig(
            @Valid @RequestBody CustomerBaseRequest request) {
        HomeScreenConfigResponse config = homeScreenConfigService.getHomeScreenConfig(request);
        return ResponseEntity.ok(ApiResponse.success(config.getTheme()));
    }
}
