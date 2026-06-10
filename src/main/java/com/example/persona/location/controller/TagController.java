package com.example.persona.location.controller;

import com.example.persona.location.dto.response.ApiResponse;
import com.example.persona.location.dto.response.TagResponse;
import com.example.persona.location.service.CategoryService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public read access to location tags (FR-03) — e.g. for CMS tag pickers and filter chips. */
@Slf4j
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Location - Categories", description = "Location categories and tags for locator filters")
public class TagController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List location tags")
    @Timed(value = "http.location.tags.list")
    public ResponseEntity<ApiResponse<List<TagResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listTags()));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get a single tag by code")
    @Timed(value = "http.location.tags.get")
    public ResponseEntity<ApiResponse<TagResponse>> get(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getTag(code)));
    }
}
