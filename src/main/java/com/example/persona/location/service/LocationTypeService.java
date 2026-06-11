package com.example.persona.location.service;

import com.example.persona.enums.StatusType;
import com.example.persona.location.dto.response.LocationTypeResponse;
import com.example.persona.location.dto.response.TagResponse;
import com.example.persona.location.exception.LocationTypeNotFoundException;
import com.example.persona.location.mapper.LocationTypeMapper;
import com.example.persona.location.repository.LocationTagRepository;
import com.example.persona.location.repository.LocationTypeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-side service for location types and tags (FR-01 / FR-03). */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationTypeService {

    private final LocationTypeRepository locationTypeRepository;
    private final LocationTagRepository tagRepository;
    private final LocationTypeMapper locationTypeMapper;

    /**
     * Active types ordered for display, each with its tags. Mapping happens
     * inside the transaction so lazily-fetched tags resolve (open-in-view is off).
     */
    @Transactional(readOnly = true)
    public List<LocationTypeResponse> listLocationTypes() {
        List<LocationTypeResponse> types = locationTypeRepository
                .findAllByStatusWithTags(StatusType.ACTIVE)
                .stream()
                .map(locationTypeMapper::toResponse)
                .toList();
        log.debug("Returning {} active location types", types.size());
        return types;
    }

    @Transactional(readOnly = true)
    public LocationTypeResponse getByCode(String code) {
        return locationTypeRepository
                .findByCode(code)
                .map(locationTypeMapper::toResponse)
                .orElseThrow(() -> LocationTypeNotFoundException.locationType(code));
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listTags() {
        return tagRepository.findByStatusOrderByCodeAsc(StatusType.ACTIVE).stream()
                .map(locationTypeMapper::toTagResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TagResponse getTag(String code) {
        return tagRepository
                .findByCode(code)
                .map(locationTypeMapper::toTagResponse)
                .orElseThrow(() -> LocationTypeNotFoundException.tag(code));
    }
}
