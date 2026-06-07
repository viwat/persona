package com.example.persona.controller;

import com.example.persona.dto.ApiResponse;
import com.example.persona.favorite.dto.FavoriteCreateRequest;
import com.example.persona.favorite.dto.FavoriteFilterRequest;
import com.example.persona.favorite.dto.FavoriteResponse;
import com.example.persona.favorite.dto.UserFavoriteCreateRequest;
import com.example.persona.favorite.dto.UserFavoriteFilterRequest;
import com.example.persona.favorite.dto.UserFavoriteModifyRequest;
import com.example.persona.favorite.dto.UserFavoriteResponse;
import com.example.persona.favorite.service.FavoriteService;
import com.example.persona.favorite.service.UserFavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorite API", description = "APIs for managing favorite data")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserFavoriteService userFavoriteService;

    @PostMapping("/create")
    public ResponseEntity<@NonNull ApiResponse<FavoriteResponse>> createFavorite(
            @RequestBody @Valid FavoriteCreateRequest request) {
        log.info("REST request to create favorite = {}", request);

        FavoriteResponse favoriteResponse = favoriteService.createFavorite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(favoriteResponse));
    }

    @GetMapping("/list")
    public ResponseEntity<@NonNull ApiResponse<List<FavoriteResponse>>> getAllFavorites() {
        log.info("REST request to get all favorites");

        List<FavoriteResponse> favoriteResponses = favoriteService.getAllFavorites();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(favoriteResponses));
    }

    @GetMapping("/find-by-service")
    public ResponseEntity<@NonNull ApiResponse<FavoriteResponse>> getFavoriteByServiceTypeAndServiceCode(
            @RequestParam String serviceType, @RequestParam String serviceCode) {
        FavoriteResponse favoriteResponse =
                favoriteService.getFavoriteByServiceTypeAndServiceCode(serviceType, serviceCode);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(favoriteResponse));
    }

    @GetMapping("/find-by-service-version")
    public ResponseEntity<@NonNull ApiResponse<List<FavoriteResponse>>> getFavoritesByServiceTypeAndVersion(
            @RequestParam String serviceType, @RequestParam String version) {
        List<FavoriteResponse> favoriteResponses =
                favoriteService.getFavoritesByServiceTypeAndVersion(serviceType, version);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(favoriteResponses));
    }

    @PostMapping("/filter")
    public ResponseEntity<@NonNull ApiResponse<List<FavoriteResponse>>> getFavoritesByFilter(
            @RequestBody FavoriteFilterRequest filter) {
        List<FavoriteResponse> favoriteResponses = favoriteService.getFavoritesByFilter(filter);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(favoriteResponses));
    }

    /**
     * Create a new user favorite
     */
    @PostMapping("/user-favorites/create")
    public ResponseEntity<@NonNull ApiResponse<UserFavoriteResponse>> createUserFavorite(
            @RequestBody @Valid UserFavoriteCreateRequest request) {
        log.info("REST request to create user favorite for customerKey={}", request.getCustomerKey());

        UserFavoriteResponse response = userFavoriteService.createUserFavorite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * Modify an existing user favorite
     */
    @PutMapping("/user-favorites/{userFavoriteId}/modify")
    public ResponseEntity<@NonNull ApiResponse<UserFavoriteResponse>> modifyUserFavorite(
            @PathVariable Long userFavoriteId, @RequestBody @Valid UserFavoriteModifyRequest request) {
        log.info(
                "REST request to modify userFavoriteId={} for customerKey={}",
                userFavoriteId,
                request.getCustomerKey());

        UserFavoriteResponse userFavorite = userFavoriteService.modifyUserFavorite(userFavoriteId, request);

        return ResponseEntity.ok(ApiResponse.success(userFavorite));
    }

    /**
     * Toggle pin/unpin favorite
     */
    @PatchMapping("/user-favorites/{userFavoriteId}/toggle-pin")
    public ResponseEntity<@NonNull ApiResponse<UserFavoriteResponse>> togglePinStatus(
            @PathVariable Long userFavoriteId, @RequestParam String customerKey) {
        log.info("REST request to toggle pin for favoriteId={} customerKey={}", userFavoriteId, customerKey);

        UserFavoriteResponse userFavoriteResponse =
                userFavoriteService.toggleUserFavoritePinStatus(userFavoriteId, customerKey);

        return ResponseEntity.ok(ApiResponse.success(userFavoriteResponse));
    }

    /**
     * Reorder favorite
     */
    @PatchMapping("/user-favorites/{userFavoriteId}/reorder")
    ResponseEntity<@NonNull ApiResponse<UserFavoriteResponse>> reorderUserFavorites(
            @PathVariable Long userFavoriteId, @RequestParam String customerKey, @RequestParam Integer newOrder) {
        log.info("REST request to reorder favoriteId={} to position={}", userFavoriteId, newOrder);

        return ResponseEntity.ok(ApiResponse.success(
                userFavoriteService.reorderUserFavorites(null, userFavoriteId, customerKey, newOrder)));
    }

    /**
     * Soft delete favorite
     */
    @PostMapping("/user-favorites/{userFavoriteId}/delete")
    public ResponseEntity<@NonNull ApiResponse<UserFavoriteResponse>> deleteUserFavorite(
            @PathVariable Long userFavoriteId, @RequestParam String customerKey) {
        log.info("REST request to delete favoriteId={} customerKey={}", userFavoriteId, customerKey);

        UserFavoriteResponse response = userFavoriteService.deleteUserFavorite(userFavoriteId, customerKey);
        return response != null
                ? ResponseEntity.ok(ApiResponse.success(response))
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/user-favorites/{userFavoriteId}/find")
    public ResponseEntity<@NonNull ApiResponse<UserFavoriteResponse>> getUserFavoriteById(
            @PathVariable Long userFavoriteId, @RequestParam String customerKey) {
        log.info("REST request to get user favorite for customerKey={}", customerKey);

        return ResponseEntity.ok(
                ApiResponse.success(userFavoriteService.getUserFavoriteById(userFavoriteId, customerKey)));
    }

    /**
     * Get all active favorites for user
     */
    @GetMapping("/user-favorites/list")
    public ResponseEntity<@NonNull ApiResponse<List<UserFavoriteResponse>>> getUserFavoritesByCustomerNumber(
            @RequestParam String customerNumber) {
        log.info("REST request to get all user favorites for customerNumber={}", customerNumber);

        return ResponseEntity.ok(
                ApiResponse.success(userFavoriteService.getUserFavoritesByCustomerNumber(customerNumber)));
    }

    @PostMapping("/user-favorites/filter")
    public ResponseEntity<@NonNull ApiResponse<List<UserFavoriteResponse>>> getUserFavoritesByCustomerNumber(
            @RequestBody @Valid UserFavoriteFilterRequest filter) {
        log.info("REST request to get all user favorites for filter={}", filter);

        return ResponseEntity.ok(ApiResponse.success(userFavoriteService.getUserFavoritesByFilter(filter)));
    }
}
