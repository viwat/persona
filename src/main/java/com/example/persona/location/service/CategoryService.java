package com.example.persona.location.service;

import com.example.persona.enums.StatusType;
import com.example.persona.location.dto.response.CategoryResponse;
import com.example.persona.location.dto.response.TagResponse;
import com.example.persona.location.exception.CategoryNotFoundException;
import com.example.persona.location.mapper.LocationCategoryMapper;
import com.example.persona.location.repository.LocationCategoryRepository;
import com.example.persona.location.repository.LocationTagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-side service backing {@code GET /categories/list} (FR-01 / FR-03). */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final LocationCategoryRepository categoryRepository;
    private final LocationTagRepository tagRepository;
    private final LocationCategoryMapper categoryMapper;

    /**
     * Active categories ordered for display, each with its tags. Mapping happens
     * inside the transaction so lazily-fetched tags resolve (open-in-view is off).
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        List<CategoryResponse> categories = categoryRepository.findAllByStatusWithTags(StatusType.ACTIVE).stream()
                .map(categoryMapper::toResponse)
                .toList();
        log.debug("Returning {} active location categories", categories.size());
        return categories;
    }

    @Transactional(readOnly = true)
    public CategoryResponse getByCode(String code) {
        return categoryRepository
                .findByCode(code)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> CategoryNotFoundException.category(code));
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listTags() {
        return tagRepository.findByStatusOrderByCodeAsc(StatusType.ACTIVE).stream()
                .map(categoryMapper::toTagResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TagResponse getTag(String code) {
        return tagRepository
                .findByCode(code)
                .map(categoryMapper::toTagResponse)
                .orElseThrow(() -> CategoryNotFoundException.tag(code));
    }
}
