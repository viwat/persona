package com.example.persona.favorite.service;

import com.example.persona.enums.StatusType;
import com.example.persona.exception.AppException;
import com.example.persona.favorite.dto.UserFavoriteCreateRequest;
import com.example.persona.favorite.dto.UserFavoriteFilterRequest;
import com.example.persona.favorite.dto.UserFavoriteModifyRequest;
import com.example.persona.favorite.dto.UserFavoriteResponse;
import com.example.persona.favorite.mapper.UserFavoriteHistoryMapper;
import com.example.persona.favorite.mapper.UserFavoriteMapper;
import com.example.persona.favorite.model.Favorite;
import com.example.persona.favorite.model.UserFavorite;
import com.example.persona.favorite.repository.FavoriteRepository;
import com.example.persona.favorite.repository.UserFavoriteHistoryRepository;
import com.example.persona.favorite.repository.UserFavoriteRepository;
import com.example.persona.favorite.spec.UserFavoriteSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final UserFavoriteHistoryRepository userFavoriteHistoryRepository;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserFavoriteHistoryMapper userFavoriteHistoryMapper;

    @Transactional
    public UserFavoriteResponse createUserFavorite(UserFavoriteCreateRequest request) {
        log.info(
                "Creating user favorite for customerKey: {} with serviceType: {}",
                request.getCustomerKey(),
                request.getServiceType());

        // Find the favorite template
        Favorite favorite = favoriteRepository
                .findByServiceTypeAndServiceCode(request.getServiceType(), request.getServiceCode())
                .orElseThrow(() -> new AppException("Favorite template not found for service type: "
                        + request.getServiceType() + " and service code: " + request.getServiceCode()));

        // Create UserFavorite
        UserFavorite userFavorite = userFavoriteMapper.toEntity(request);
        userFavorite.setFavorite(favorite);
        userFavorite.setFavoriteId(favorite.getId());
        userFavorite.setServiceIcon(favorite.getServiceIcon());
        userFavorite.setPinned(false);
        userFavorite.setDisplayOrder(getNextDisplayOrder(request.getCustomerKey()));
        UserFavorite savedUserFavorite = userFavoriteRepository.save(userFavorite);
        log.info("Successfully created user's favorite with ID: {}", savedUserFavorite.getId());
        userFavoriteHistoryRepository.save(userFavoriteHistoryMapper.fromUserFavorite(savedUserFavorite));
        return userFavoriteMapper.toResponse(savedUserFavorite);
    }

    @Transactional
    public UserFavoriteResponse modifyUserFavorite(Long userFavoriteId, UserFavoriteModifyRequest request) {
        log.info("Modifying user's favorite ID: {} for customerKey: {}", userFavoriteId, request.getCustomerKey());
        // Find and soft delete the existing favorite
        UserFavorite existingFavorite = userFavoriteRepository
                .findByIdAndServiceTypeAndServiceCodeAndCustomerKeyAndStatus(
                        userFavoriteId,
                        request.getServiceType(),
                        request.getServiceCode(),
                        request.getCustomerKey(),
                        StatusType.ACTIVE)
                .orElseThrow(() -> new AppException(String.format(
                        "User favorite not found with ID %s and ServiceType %s and ServiceCode %s",
                        userFavoriteId, request.getServiceType(), request.getServiceCode())));
        // Soft delete existing favorite and its attributes
        existingFavorite.setStatus(StatusType.MODIFIED);
        userFavoriteRepository.save(existingFavorite);
        // Write to history
        userFavoriteHistoryRepository.save(userFavoriteHistoryMapper.fromUserFavorite(existingFavorite));
        // Create new favorite with updated information
        // Maintain the same display order from the existing favorite
        request.setDisplayOrder(existingFavorite.getDisplayOrder());
        existingFavorite.setStatus(StatusType.ACTIVE);
        // Create new record
        userFavoriteMapper.updateEntityFromRequest(request, existingFavorite);
        existingFavorite = userFavoriteRepository.save(existingFavorite);
        return userFavoriteMapper.toResponse(existingFavorite);
    }

    @Transactional
    public UserFavoriteResponse deleteUserFavorite(Long userFavoriteId, String customerKey) {
        log.info("Deleting user's favorite ID: {} for customerKey: {}", userFavoriteId, customerKey);
        // Find and soft delete the existing favorite
        UserFavorite existingFavorite = userFavoriteRepository
                .findByIdAndCustomerKeyAndStatus(userFavoriteId, customerKey, StatusType.ACTIVE)
                .orElseThrow(() -> new AppException("User favorite not found with ID: " + userFavoriteId));
        // Soft delete existing favorite and its attributes
        existingFavorite.setStatus(StatusType.DELETED);
        userFavoriteRepository.save(existingFavorite);
        // Write to history
        userFavoriteHistoryRepository.save(userFavoriteHistoryMapper.fromUserFavorite(existingFavorite));
        return userFavoriteMapper.toResponse(existingFavorite);
    }

    @Transactional
    public UserFavoriteResponse toggleUserFavoritePinStatus(Long userFavoriteId, String customerKey) {
        log.info("Toggling pin status for user favorite: {} and customerKey: {}", userFavoriteId, customerKey);
        UserFavorite userFavorite = userFavoriteRepository
                .findByIdAndCustomerKeyAndStatus(userFavoriteId, customerKey, StatusType.ACTIVE)
                .orElseThrow(() -> new AppException("User favorite not found with ID: " + userFavoriteId));
        if (!userFavorite.isPinned()) {
            userFavorite.setPinned(true);
            reorderUserFavorites(userFavorite, userFavoriteId, customerKey, Integer.MAX_VALUE); // bottom of pinned
        } else {
            userFavorite.setPinned(false);
            reorderUserFavorites(userFavorite, userFavoriteId, customerKey, 1); // top of unpinned
        }
        return userFavoriteMapper.toResponse(userFavorite);
    }

    @Transactional
    public UserFavoriteResponse reorderUserFavorites(
            UserFavorite target, Long userFavoriteId, String customerKey, Integer newOrder) {
        UserFavorite userFavorite = target != null
                ? target
                : userFavoriteRepository
                        .findByIdAndCustomerKeyAndStatus(userFavoriteId, customerKey, StatusType.ACTIVE)
                        .orElseThrow(() -> new AppException("User favorite not found"));

        List<UserFavorite> all =
                userFavoriteRepository.findByCustomerKeyAndStatusOrderByDisplayOrder(customerKey, StatusType.ACTIVE);

        List<UserFavorite> pinned = all.stream()
                .filter(UserFavorite::isPinned)
                .filter(s -> !s.getId().equals(userFavorite.getId()))
                .toList();

        List<UserFavorite> unpinned = all.stream()
                .filter(s -> !s.isPinned())
                .filter(s -> !s.getId().equals(userFavorite.getId()))
                .toList();

        List<UserFavorite> targetList = new ArrayList<>(userFavorite.isPinned() ? pinned : unpinned);

        int max = targetList.size();
        int position = Math.max(1, Math.min(newOrder, max + 1));

        targetList.add(position - 1, userFavorite);

        List<UserFavorite> finalList = new ArrayList<>();

        if (userFavorite.isPinned()) {
            finalList.addAll(targetList); // pinned with target inserted
            finalList.addAll(unpinned);
        } else {
            finalList.addAll(pinned);
            finalList.addAll(targetList); // unpinned with target inserted
        }

        for (int i = 0; i < finalList.size(); i++) {
            finalList.get(i).setDisplayOrder(i + 1);
        }

        userFavoriteRepository.saveAll(finalList);
        return userFavoriteMapper.toResponse(userFavorite);
    }

    @Transactional
    public UserFavoriteResponse getUserFavoriteById(Long userFavoriteId, String customerKey) {
        log.info("Find user favorite for user favorite: {} and customerKey: {}", userFavoriteId, customerKey);
        UserFavorite userFavorite = userFavoriteRepository
                .findByIdAndCustomerKeyAndStatus(userFavoriteId, customerKey, StatusType.ACTIVE)
                .orElseThrow(() -> new AppException("User favorite not found with ID: " + userFavoriteId));
        return userFavoriteMapper.toResponse(userFavorite);
    }

    @Transactional(readOnly = true)
    public List<UserFavoriteResponse> getUserFavoritesByCustomerNumber(String customerNumber) {
        log.info("Fetching all active favorites for customerKey: {}", customerNumber);
        return userFavoriteRepository
                .findByCustomerNoAndStatusOrderByDisplayOrder(customerNumber, StatusType.ACTIVE)
                .stream()
                .map(userFavoriteMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Integer getNextDisplayOrder(String customerKey) {
        return userFavoriteRepository
                .findMaxDisplayOrderByCustomerKey(customerKey)
                .map(maxOrder -> maxOrder + 1)
                .orElse(1);
    }

    @Transactional(readOnly = true)
    public List<UserFavoriteResponse> getUserFavoritesByFilter(UserFavoriteFilterRequest filter) {
        List<UserFavorite> userFavorites = userFavoriteRepository.findAll(UserFavoriteSpecification.filter(filter));
        return userFavorites.stream().map(userFavoriteMapper::toResponse).collect(Collectors.toList());
    }
}
