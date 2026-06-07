package com.example.persona.controller;

import com.example.persona.dto.ApiResponse;
import com.example.persona.i18n.dto.request.TranslationCreateRequest;
import com.example.persona.i18n.dto.request.TranslationModifyRequest;
import com.example.persona.i18n.dto.response.TranslationResponse;
import com.example.persona.i18n.service.TranslationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/translations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Translation API", description = "APIs for managing translation data")
public class TranslationController {

    private final TranslationService translationService;

    /**
     * Create a new translation
     */
    @PostMapping("/create")
    public ResponseEntity<@NonNull ApiResponse<TranslationResponse>> createTranslation(
            @Valid @RequestBody TranslationCreateRequest request) {
        log.debug("Received create translation request: {}", request);
        TranslationResponse response = translationService.createTranslation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * Modify an existing translation
     */
    @PutMapping("/modify")
    public ResponseEntity<@NonNull ApiResponse<TranslationResponse>> modifyTranslation(
            @Valid @RequestBody TranslationModifyRequest request) {
        log.debug("Received modify translation request: {}", request);
        TranslationResponse response = translationService.modifyTranslation(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all active translations for an app
     */
    @GetMapping("/find-by-appcode")
    public ResponseEntity<@NonNull ApiResponse<List<TranslationResponse>>> getTranslations(
            @RequestParam String appCode) {
        log.debug("Fetching translations for appCode: {}", appCode);
        List<TranslationResponse> translations = translationService.getTranslations(appCode);
        return ResponseEntity.ok(ApiResponse.success(translations));
    }

    /**
     * Get a single translation by code
     */
    @GetMapping("/find-by-code")
    public ResponseEntity<@NonNull String> getTranslation(
            @RequestParam String code, @RequestParam(required = false) String defaultValue) {
        if (defaultValue == null) {
            log.debug("Fetching translation for code: {}", code);
            String translation = translationService.getTranslation(code);
            return ResponseEntity.ok(translation);
        } else {
            String translation = translationService.getTranslationOrDefault(code, defaultValue);
            return ResponseEntity.ok(translation);
        }
    }

    /**
     * Delete a translation
     */
    @PostMapping("/{id}/delete")
    public ResponseEntity<@NonNull Void> deleteTranslation(@PathVariable Long id) {
        log.debug("Deleting translation with id: {}", id);
        translationService.deleteTranslation(id);
        return ResponseEntity.noContent().build();
    }
}
