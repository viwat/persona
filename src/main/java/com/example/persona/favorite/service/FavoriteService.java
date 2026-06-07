package com.example.persona.favorite.service;

import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.favorite.dto.FavoriteCreateRequest;
import com.example.persona.favorite.dto.FavoriteFilterRequest;
import com.example.persona.favorite.dto.FavoriteResponse;
import com.example.persona.favorite.mapper.FavoriteMapper;
import com.example.persona.favorite.model.Favorite;
import com.example.persona.favorite.repository.FavoriteRepository;
import com.example.persona.favorite.spec.FavoriteSpecification;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final FavoriteRepository favoriteRepository;

    @Transactional
    public FavoriteResponse createFavorite(FavoriteCreateRequest request) {
        log.info(
                "Creating favorite for serviceType: {} with serviceCode: {}",
                request.getServiceType(),
                request.getServiceCode());
        // Find the favorite template
        Optional<Favorite> favorite =
                favoriteRepository.findByServiceTypeAndServiceCode(request.getServiceType(), request.getServiceCode());
        if (favorite.isPresent()) {
            throw new BusinessException("Favorite template is already existed for service_type: "
                    + request.getServiceType() + " and service_code: " + request.getServiceCode());
        }
        Favorite favoriteObj = favoriteMapper.toEntity(request);
        favoriteRepository.save(favoriteObj);
        return favoriteMapper.toResponse(favoriteObj);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getAllFavorites() {
        List<Favorite> favorites = favoriteRepository.findAll();
        return favorites.stream()
                .filter(f -> Objects.nonNull(f.getStatus()) && f.getStatus().equals(StatusType.ACTIVE))
                .map(favoriteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FavoriteResponse getFavoriteByServiceTypeAndServiceCode(String serviceType, String serviceCode) {
        log.info("Fetching favorites for serviceType: {} and serviceCode: {}", serviceType, serviceCode);

        Favorite favorite = favoriteRepository
                .findByServiceTypeAndServiceCode(serviceType, serviceCode)
                .filter(f -> Objects.nonNull(f.getStatus()) && f.getStatus().equals(StatusType.ACTIVE))
                .orElseThrow(() -> new BusinessException(
                        "No favorite found for serviceType: " + serviceType + " and serviceCode: " + serviceCode));

        return favoriteMapper.toResponse(favorite);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavoritesByFilter(FavoriteFilterRequest filter) {
        log.info("Fetching favorites for request: {}", filter);

        Specification<@NonNull Favorite> spec = FavoriteSpecification.filter(filter);

        List<Favorite> favorites = favoriteRepository.findAll(
                spec,
                Sort.by("serviceType").ascending().and(Sort.by("displayName").ascending()));

        return favorites.stream().map(favoriteMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavoritesByServiceTypeAndVersion(String serviceType, String appVersion) {
        log.info("Fetching favorites for serviceType: {} and appVersion: {}", serviceType, appVersion);

        List<Favorite> favorites = favoriteRepository.findByServiceTypeAndAppVersion(serviceType, appVersion);

        if (favorites.isEmpty()) {
            throw new BusinessException(
                    "No favorite found for service type: " + serviceType + " and version: " + appVersion);
        }

        return favorites.stream()
                .filter(f -> Objects.nonNull(f.getStatus()) && f.getStatus().equals(StatusType.ACTIVE))
                .map(favoriteMapper::toResponse)
                .collect(Collectors.toList());
    }
}
