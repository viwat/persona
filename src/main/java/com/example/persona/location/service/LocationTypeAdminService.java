package com.example.persona.location.service;

import com.example.persona.enums.StatusType;
import com.example.persona.location.dto.request.LocationTypeRequest.LocationTypeUpsertRequest;
import com.example.persona.location.dto.request.LocationTypeRequest.TagUpsertRequest;
import com.example.persona.location.dto.response.LocationTypeResponse;
import com.example.persona.location.dto.response.TagResponse;
import com.example.persona.location.exception.LocationTypeNotFoundException;
import com.example.persona.location.mapper.LocationTypeMapper;
import com.example.persona.location.model.LocationTag;
import com.example.persona.location.model.LocationType;
import com.example.persona.location.repository.LocationTagRepository;
import com.example.persona.location.repository.LocationTypeRepository;
import com.example.persona.model.MultilingualContent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin write operations for location types and tags (FR-03 Website Locator Management). */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LocationTypeAdminService {

    private final LocationTypeRepository locationTypeRepository;
    private final LocationTagRepository tagRepository;
    private final LocationTypeMapper mapper;

    // ── Location Types ──────────────────────────────────────────────────────────

    public LocationTypeResponse createLocationType(LocationTypeUpsertRequest request, String actor) {
        if (locationTypeRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Location type already exists: " + request.code());
        }
        LocationType locationType = LocationType.builder()
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
        LocationType saved = locationTypeRepository.save(locationType);
        log.info("Created location type: code={}, actor={}", saved.getCode(), actor);
        return mapper.toResponse(saved);
    }

    public LocationTypeResponse updateLocationType(String code, LocationTypeUpsertRequest request, String actor) {
        LocationType locationType = locationTypeRepository
                .findByCode(code)
                .orElseThrow(() -> LocationTypeNotFoundException.locationType(code));
        locationType.setName(multilingual(request.name(), request.nameKhmer()));
        locationType.setDescription(multilingual(request.description(), request.descriptionKhmer()));
        locationType.setMarkIcon(request.markIcon());
        locationType.setMarkIconUrl(request.markIconUrl());
        locationType.setDisplayOrder(request.displayOrder());
        locationType.setTags(resolveTags(request.tagCodes()));
        locationType.setModifiedBy(actor);
        log.info("Updated location type: code={}, actor={}", code, actor);
        return mapper.toResponse(locationTypeRepository.save(locationType));
    }

    /** Soft-delete: status flips to DELETED, so it drops out of the active type list. */
    public void deleteLocationType(String code, String actor) {
        LocationType locationType = locationTypeRepository
                .findByCode(code)
                .orElseThrow(() -> LocationTypeNotFoundException.locationType(code));
        locationType.setStatus(StatusType.DELETED);
        locationType.setModifiedBy(actor);
        locationTypeRepository.save(locationType);
        log.info("Soft-deleted location type: code={}, actor={}", code, actor);
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
        LocationTag tag =
                tagRepository.findByCode(code).orElseThrow(() -> LocationTypeNotFoundException.tag(code));
        tag.setName(multilingual(request.name(), request.nameKhmer()));
        tag.setModifiedBy(actor);
        log.info("Updated location tag: code={}, actor={}", code, actor);
        return mapper.toTagResponse(tagRepository.save(tag));
    }

    /** Hard-delete: the type↔tag join rows are removed by the FK ON DELETE CASCADE. */
    public void deleteTag(String code, String actor) {
        LocationTag tag =
                tagRepository.findByCode(code).orElseThrow(() -> LocationTypeNotFoundException.tag(code));
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
            tags.add(tagRepository.findByCode(code).orElseThrow(() -> LocationTypeNotFoundException.tag(code)));
        }
        return tags;
    }

    private MultilingualContent multilingual(String en, String km) {
        if (en == null && km == null) return null;
        return MultilingualContent.builder().en(en).km(km).build();
    }
}
