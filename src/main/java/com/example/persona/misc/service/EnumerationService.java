package com.example.persona.misc.service;

import com.example.persona.enums.StatusType;
import com.example.persona.exception.AppException;
import com.example.persona.misc.dto.request.EnumerationCreateRequest;
import com.example.persona.misc.dto.request.EnumerationModifyRequest;
import com.example.persona.misc.dto.response.EnumerationResponse;
import com.example.persona.misc.mapper.EnumerationMapper;
import com.example.persona.misc.model.Enumeration;
import com.example.persona.misc.repository.EnumerationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnumerationService {
    private final EnumerationMapper enumerationMapper;
    private final EnumerationRepository enumerationRepository;

    private final RedisTemplate<String, Enumeration> enumerationRedisTemplate;

    @Transactional
    public EnumerationResponse createEnumeration(EnumerationCreateRequest request) {
        Enumeration enumeration = enumerationMapper.toEntity(request);
        enumeration.setStatus(StatusType.ACTIVE);
        enumerationRepository.save(enumeration);
        return enumerationMapper.toResponse(enumeration);
    }

    @Transactional
    public EnumerationResponse modifyEnumeration(Long id, EnumerationModifyRequest request) {
        Enumeration enumeration =
                enumerationRepository.findById(id).orElseThrow(() -> new AppException("Enumeration Not Found"));
        enumerationMapper.updateEntityFromRequest(request, enumeration);
        enumeration.setModifiedDate(LocalDateTime.now());
        enumerationRepository.save(enumeration);
        return enumerationMapper.toResponse(enumeration);
    }

    @Transactional(readOnly = true)
    public EnumerationResponse getEnumeration(Long id) {
        Enumeration enumeration =
                enumerationRepository.findById(id).orElseThrow(() -> new AppException("Enumeration Not Found"));
        return enumerationMapper.toResponse(enumeration);
    }

    @Transactional(readOnly = true)
    public List<EnumerationResponse> getEnumerations(String code) {
        List<Enumeration> enumerations = enumerationRepository.findByCodeOrderByDisplayOrderAsc(code);
        return enumerations.stream().map(enumerationMapper::toResponse).collect(Collectors.toList());
    }
}
