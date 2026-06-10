package com.example.persona.location.service;

import com.example.persona.enums.StatusType;
import com.example.persona.location.dto.request.CategoryRequest.CategoryUpsertRequest;
import com.example.persona.location.dto.request.CategoryRequest.TagUpsertRequest;
import com.example.persona.location.dto.response.CategoryResponse;
import com.example.persona.location.dto.response.TagResponse;
import com.example.persona.location.exception.CategoryNotFoundException;
import com.example.persona.location.mapper.LocationCategoryMapper;
import com.example.persona.location.model.LocationCategory;
import com.example.persona.location.model.LocationTag;
import com.example.persona.location.repository.LocationCategoryRepository;
import com.example.persona.location.repository.LocationTagRepository;
import com.example.persona.model.MultilingualContent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin write operations for location categories and tags (FR-03 Website Locator Management). */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryAdminService {

    private final LocationCategoryRepository categoryRepository;
    private final LocationTagRepository tagRepository;
    private final LocationCategoryMapper mapper;

    // ── Categories ─────────────────────────────────────────────────────────────

    public CategoryResponse createCategory(CategoryUpsertRequest request, String actor) {
        if (categoryRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Category already exists: " + request.code());
        }
        LocationCategory category = LocationCategory.builder()
                .code(request.code())
                .name(multilingual(request.name(), request.nameKhmer()))
                .description(multilingual(request.description(), request.descriptionKhmer()))
                .markIcon(request.markIcon())
                .markIconUrl(request.markIconUrl())
                .displayOrder(request.displayOrder())
                .tags(resolveTags(request.tagCodes()))
                .status(StatusType.ACTIVE)
                .createdBy(actor)
                .modifiedBy(actor)
                .build();
        LocationCategory saved = categoryRepository.save(category);
        log.info("Created location category: code={}, actor={}", saved.getCode(), actor);
        return mapper.toResponse(saved);
    }

    public CategoryResponse updateCategory(String code, CategoryUpsertRequest request, String actor) {
        LocationCategory category =
                categoryRepository.findByCode(code).orElseThrow(() -> CategoryNotFoundException.category(code));
        category.setName(multilingual(request.name(), request.nameKhmer()));
        category.setDescription(multilingual(request.description(), request.descriptionKhmer()));
        category.setMarkIcon(request.markIcon());
        category.setMarkIconUrl(request.markIconUrl());
        category.setDisplayOrder(request.displayOrder());
        category.setTags(resolveTags(request.tagCodes()));
        category.setModifiedBy(actor);
        log.info("Updated location category: code={}, actor={}", code, actor);
        return mapper.toResponse(categoryRepository.save(category));
    }

    /** Soft-delete: status flips to DELETED, so it drops out of the active category list. */
    public void deleteCategory(String code, String actor) {
        LocationCategory category =
                categoryRepository.findByCode(code).orElseThrow(() -> CategoryNotFoundException.category(code));
        category.setStatus(StatusType.DELETED);
        category.setModifiedBy(actor);
        categoryRepository.save(category);
        log.info("Soft-deleted location category: code={}, actor={}", code, actor);
    }

    // ── Tags ────────────────────────────────────────────────────────────────────

    public TagResponse createTag(TagUpsertRequest request, String actor) {
        if (tagRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Tag already exists: " + request.code());
        }
        LocationTag tag = LocationTag.builder()
                .code(request.code())
                .name(multilingual(request.name(), request.nameKhmer()))
                .status(StatusType.ACTIVE)
                .createdBy(actor)
                .modifiedBy(actor)
                .build();
        LocationTag saved = tagRepository.save(tag);
        log.info("Created location tag: code={}, actor={}", saved.getCode(), actor);
        return mapper.toTagResponse(saved);
    }

    public TagResponse updateTag(String code, TagUpsertRequest request, String actor) {
        LocationTag tag = tagRepository.findByCode(code).orElseThrow(() -> CategoryNotFoundException.tag(code));
        tag.setName(multilingual(request.name(), request.nameKhmer()));
        tag.setModifiedBy(actor);
        log.info("Updated location tag: code={}, actor={}", code, actor);
        return mapper.toTagResponse(tagRepository.save(tag));
    }

    /** Hard-delete: the category↔tag join rows are removed by the FK ON DELETE CASCADE. */
    public void deleteTag(String code, String actor) {
        LocationTag tag = tagRepository.findByCode(code).orElseThrow(() -> CategoryNotFoundException.tag(code));
        tagRepository.delete(tag);
        log.info("Deleted location tag: code={}, actor={}", code, actor);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private Set<LocationTag> resolveTags(List<String> tagCodes) {
        if (tagCodes == null || tagCodes.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<LocationTag> tags = new LinkedHashSet<>();
        for (String code : tagCodes) {
            tags.add(tagRepository.findByCode(code).orElseThrow(() -> CategoryNotFoundException.tag(code)));
        }
        return tags;
    }

    /** Builds a {@link MultilingualContent} (English + Khmer); returns null when both are absent. */
    private MultilingualContent multilingual(String en, String km) {
        if (en == null && km == null) return null;
        return MultilingualContent.builder().en(en).km(km).build();
    }
}
