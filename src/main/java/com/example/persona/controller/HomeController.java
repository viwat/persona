package com.example.persona.controller;

import com.example.persona.catalog.dto.response.CategoryProductsResponse;
import com.example.persona.dto.ApiResponse;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.dto.response.HomeScreenMetadata;
import com.example.persona.home.dto.AnnouncementResponse;
import com.example.persona.home.dto.GreetingResponse;
import com.example.persona.home.dto.HomeScreenConfigResponse;
import com.example.persona.home.dto.PromotionResponse;
import com.example.persona.home.dto.UpdateBannerResponse;
import com.example.persona.home.service.AppUpdateService;
import com.example.persona.home.service.GreetingService;
import com.example.persona.home.service.HomeScreenConfigService;
import com.example.persona.home.service.PromotionService;
import com.example.persona.service.HomeService;
import com.example.persona.service.UpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
@Tag(name = "Home Screen API", description = "APIs for home screen product catalog")
public class HomeController {

    private final HomeService homeService;
    private final UpdateService updateService;
    private final PromotionService promotionService;
    private final HomeScreenConfigService homeScreenConfigService;
    private final GreetingService greetingService;
    private final AppUpdateService appUpdateService;

    @Operation(summary = "Get all products grouped by categories")
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<List<CategoryProductsResponse>>> getAllProducts(
            @RequestBody @Valid CustomerBaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(homeService.getAllProducts(request)));
    }

    @Operation(summary = "Check for home screen updates")
    @GetMapping("/check-updates/{event}")
    public ResponseEntity<ApiResponse<HomeScreenMetadata>> checkUpdates(
            @PathVariable String event, @RequestBody @Valid CustomerBaseRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(updateService.checkUpdates(LocalDateTime.now(), request.getCustomerKey(), event)));
    }

    @Operation(summary = "Get home screen metadata")
    @PostMapping("/metadata")
    public ResponseEntity<ApiResponse<HomeScreenMetadata>> getMetadata(
            @RequestBody @Valid CustomerBaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(homeService.getMetadata(request)));
    }

    @Operation(summary = "Get active announcements for customer")
    @PostMapping("/announcements")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getAnnouncements(
            @RequestBody @Valid CustomerBaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.getAnnouncement(request)));
    }

    @Operation(summary = "Get active promotions for customer")
    @PostMapping("/promotions")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getPromotions(
            @RequestBody @Valid CustomerBaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.getPromotion(request)));
    }

    @Operation(
            summary = "Get home screen configuration",
            description =
                    "Returns complete home screen configuration including theme, greeting, services, navigation, and update banner")
    @PostMapping("/config")
    public ResponseEntity<ApiResponse<HomeScreenConfigResponse>> getHomeScreenConfig(
            @RequestBody @Valid CustomerBaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(homeScreenConfigService.getHomeScreenConfig(request)));
    }

    @Operation(
            summary = "Get personalized greeting",
            description = "Returns personalized greeting with user name and time-based greeting")
    @PostMapping("/greeting")
    public ResponseEntity<ApiResponse<GreetingResponse>> getGreeting(@RequestBody @Valid CustomerBaseRequest request) {
        String customerNo = request.getCustomerNo();
        if (customerNo == null) {
            customerNo = request.getCustomerKey();
        }
        return ResponseEntity.ok(ApiResponse.success(greetingService.getGreeting(customerNo)));
    }

    @Operation(summary = "Check app update availability", description = "Checks if a new app version is available")
    @PostMapping("/update-check")
    public ResponseEntity<ApiResponse<UpdateBannerResponse>> checkAppUpdate(
            @RequestBody @Valid CustomerBaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(appUpdateService.checkUpdate(request)));
    }
}
