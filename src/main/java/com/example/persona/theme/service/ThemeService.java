package com.example.persona.theme.service;

import com.example.persona.enums.StatusType;
import com.example.persona.exception.AppException;
import com.example.persona.exception.BusinessException;
import com.example.persona.theme.dto.request.AccentColorCreateRequest;
import com.example.persona.theme.dto.request.ThemeCategoryCreateRequest;
import com.example.persona.theme.dto.request.ThemeCategoryModifyRequest;
import com.example.persona.theme.dto.request.ThemeCreateRequest;
import com.example.persona.theme.dto.request.ThemeIconSetCreateRequest;
import com.example.persona.theme.dto.request.ThemeIconSetModifyRequest;
import com.example.persona.theme.dto.request.ThemePackageCreateRequest;
import com.example.persona.theme.dto.request.ThemePackageModifyRequest;
import com.example.persona.theme.dto.request.ThemePreviewRequest;
import com.example.persona.theme.dto.response.AccentColorResponse;
import com.example.persona.theme.dto.response.ThemeCategoryResponse;
import com.example.persona.theme.dto.response.ThemeDownloadResponse;
import com.example.persona.theme.dto.response.ThemeIconSetResponse;
import com.example.persona.theme.dto.response.ThemePackageResponse;
import com.example.persona.theme.dto.response.ThemePreviewListResponse;
import com.example.persona.theme.dto.response.ThemePreviewResponse;
import com.example.persona.theme.dto.response.ThemeResponse;
import com.example.persona.theme.event.ThemeInstalledEvent;
import com.example.persona.theme.mapper.ThemeMapper;
import com.example.persona.theme.model.AccentColor;
import com.example.persona.theme.model.Theme;
import com.example.persona.theme.model.ThemeCategory;
import com.example.persona.theme.model.ThemeIconSet;
import com.example.persona.theme.model.ThemePackage;
import com.example.persona.theme.repository.AccentColorRepository;
import com.example.persona.theme.repository.ThemeCategoryRepository;
import com.example.persona.theme.repository.ThemeIconSetRepository;
import com.example.persona.theme.repository.ThemePackageRepository;
import com.example.persona.theme.repository.ThemeRepository;
import com.example.persona.theme.validator.ThemeValidator;
import com.example.persona.utils.LanguageUtils;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThemeService {
    private final ThemeMapper themeMapper;
    private final ThemeRepository themeRepository;
    private final ThemeIconSetRepository themeIconSetRepository;
    private final ThemePackageRepository themePackageRepository;
    private final ThemeCategoryRepository themeCategoryRepository;
    private final AccentColorRepository accentColorRepository;
    private final ThemeValidator themeValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public List<ThemeIconSetResponse> createIconSets(List<ThemeIconSetCreateRequest> request) {
        List<ThemeIconSet> themeIconSets = new ArrayList<>();
        for (ThemeIconSetCreateRequest themeIconSetCreateRequest : request) {
            ThemeIconSet icon = new ThemeIconSet();
            icon.setCode(themeIconSetCreateRequest.getCode());
            icon.setIconType(themeIconSetCreateRequest.getIconType());
            icon.setIconCategory(themeIconSetCreateRequest.getIconCategory());
            icon.setIconUrl(themeIconSetCreateRequest.getIconUrl());
            icon.setMetadata(themeIconSetCreateRequest.getMetadata());
            icon.setStatus(StatusType.ACTIVE);
            themeIconSets.add(icon);
        }
        themeIconSetRepository.saveAll(themeIconSets);
        return themeMapper.toResponse(themeIconSets);
    }

    @Transactional
    public ThemePackageResponse createThemePackage(ThemePackageCreateRequest request) {
        ThemePackage themePackage = themeMapper.toEntity(request);
        themePackage.setStatus(StatusType.ACTIVE);
        themePackage.setCreatedDate(LocalDateTime.now());
        themePackage = themePackageRepository.save(themePackage);
        return themeMapper.toResponse(themePackage);
    }

    @Transactional
    public ThemePackageResponse modifyThemePackage(Long id, ThemePackageModifyRequest request) {
        ThemePackage themePackage = themePackageRepository
                .findByIdAndStatus(id, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException("Theme Package is Not Found"));
        Map<String, ThemeIconSet> existingMap =
                themePackage.getIcons().stream().collect(Collectors.toMap(ThemeIconSet::getCode, Function.identity()));
        Set<String> requestCodes = new HashSet<>();
        for (ThemeIconSetModifyRequest req : request.getIcons()) {
            requestCodes.add(req.getCode());
            ThemeIconSet existing = existingMap.get(req.getCode());
            if (existing != null) {
                // ✅ Update in-place (ID stays same)
                existing.setIconUrl(req.getIconUrl());
                existing.setIconType(req.getIconType());
                existing.setIconCategory(req.getIconCategory());
                existing.setMetadata(req.getMetadata());
            } else {
                // ✅ New entity (ID will increase only here)
                ThemeIconSet newIcon = new ThemeIconSet();
                newIcon.setCode(req.getCode());
                newIcon.setIconUrl(req.getIconUrl());
                newIcon.setIconType(req.getIconType());
                newIcon.setIconCategory(req.getIconCategory());
                newIcon.setMetadata(req.getMetadata());
                newIcon.setStatus(StatusType.ACTIVE);
                themePackage.getIcons().add(newIcon);
            }
        }
        themePackage.getIcons().removeIf(icon -> !requestCodes.contains(icon.getCode()));

        themeMapper.updateEntityFromRequest(request, themePackage);
        themePackageRepository.save(themePackage);
        return themeMapper.toResponse(themePackage);
    }

    @Transactional
    public ThemePackageResponse findThemePackageById(Long id) {
        ThemePackage themePackage = themePackageRepository
                .findByIdAndStatus(id, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException("Theme Package is Not Found"));
        return themeMapper.toResponse(themePackage);
    }

    @Transactional
    public ThemeCategoryResponse createThemeCategory(ThemeCategoryCreateRequest request) {
        ThemeCategory category = themeMapper.toEntity(request);
        category.setStatus(StatusType.ACTIVE);
        category = themeCategoryRepository.save(category);
        return themeMapper.toResponse(category);
    }

    public ThemeCategoryResponse modifyThemeCategory(Long id, @Valid ThemeCategoryModifyRequest request) {
        if (!Objects.equals(request.getLightModeThemeId(), request.getDarkModeThemeId())) {
            List<Long> ids = Stream.of(request.getLightModeThemeId(), request.getDarkModeThemeId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            List<ThemePackage> themePackages = themePackageRepository.findByStatusAndIdIn(StatusType.ACTIVE, ids);
            if (themePackages.size() != ids.size()) {
                throw new BusinessException("One or more ThemePackages not found for provided IDs");
            }
        }
        ThemeCategory themeCategory = themeCategoryRepository
                .findByIdAndStatus(id, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException("Theme Category is Not Found"));
        themeMapper.updateEntityFromRequest(request, themeCategory);
        themeCategoryRepository.save(themeCategory);
        return themeMapper.toResponse(themeCategory);
    }

    @Transactional
    public ThemeCategoryResponse findThemeCategoryById(Long id) {
        ThemeCategory themeCategory = themeCategoryRepository
                .findByIdAndStatus(id, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException("Theme Category is Not Found"));
        return themeMapper.toResponse(themeCategory);
    }

    @Transactional
    public ThemeResponse createTheme(ThemeCreateRequest request) {
        log.info("Creating theme with code: {}", request.getCode());

        // Check if theme code already exists
        themeRepository.findByCode(request.getCode()).ifPresent(existing -> {
            throw new BusinessException(String.format("Theme with code '%s' already exists", request.getCode()));
        });

        // Validate category exists
        ThemeCategory category = themeCategoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(
                        String.format("Theme category not found with id: %d", request.getCategoryId())));

        // Validate theme packages exist if provided
        ThemePackage lightModePackage = null;
        ThemePackage darkModePackage = null;

        if (request.getLightModeThemePackageId() != null) {
            lightModePackage = themePackageRepository
                    .findById(request.getLightModeThemePackageId())
                    .orElseThrow(() -> new BusinessException(String.format(
                            "Light mode theme package not found with id: %d", request.getLightModeThemePackageId())));
            log.debug(
                    "Found light mode theme package: id={}, backgroundType={}",
                    lightModePackage.getId(),
                    lightModePackage.getBackgroundType());
        } else {
            log.warn("No light mode theme package ID provided for theme: {}", request.getCode());
        }

        if (request.getDarkModeThemePackageId() != null) {
            darkModePackage = themePackageRepository
                    .findById(request.getDarkModeThemePackageId())
                    .orElseThrow(() -> new BusinessException(String.format(
                            "Dark mode theme package not found with id: %d", request.getDarkModeThemePackageId())));
            log.debug(
                    "Found dark mode theme package: id={}, backgroundType={}",
                    darkModePackage.getId(),
                    darkModePackage.getBackgroundType());
        } else {
            log.warn("No dark mode theme package ID provided for theme: {}", request.getCode());
        }

        // Create theme entity
        Theme theme = themeMapper.toEntity(request);
        theme.setCategory(category);
        theme.setLightModeThemeId(lightModePackage);
        theme.setDarkModeThemeId(darkModePackage);
        theme.setStatus(StatusType.ACTIVE);

        log.debug(
                "Setting theme packages - Light: {}, Dark: {}",
                lightModePackage != null ? lightModePackage.getId() : "null",
                darkModePackage != null ? darkModePackage.getId() : "null");

        // Save theme
        Theme savedTheme = themeRepository.save(theme);
        log.info("Theme created successfully with id: {}, code: {}", savedTheme.getId(), savedTheme.getCode());

        // Reload theme with packages to ensure relationships are loaded
        Theme themeWithPackages =
                themeRepository.findByCodeWithPackages(savedTheme.getCode()).orElse(savedTheme);

        // Verify packages were loaded
        ThemePackage loadedLight = themeWithPackages.getLightModeThemeId();
        ThemePackage loadedDark = themeWithPackages.getDarkModeThemeId();

        log.info(
                "Theme packages after reload - Light: {}, Dark: {}",
                loadedLight != null ? loadedLight.getId() : "null",
                loadedDark != null ? loadedDark.getId() : "null");

        if (lightModePackage != null && loadedLight == null) {
            log.error("WARNING: Light mode package was set but not loaded! Package ID: {}", lightModePackage.getId());
        }
        if (darkModePackage != null && loadedDark == null) {
            log.error("WARNING: Dark mode package was set but not loaded! Package ID: {}", darkModePackage.getId());
        }

        return themeMapper.toResponse(themeWithPackages);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getAllThemes() {
        List<Theme> themes = themeRepository.findAll();
        return themeMapper.toThemeResponses(themes);
    }

    @Transactional(readOnly = true)
    public ThemePreviewListResponse getActiveThemes(ThemePreviewRequest request) {
        LocalDate currentDate = LocalDate.now();
        String customerSegment = request.getCustomerSegment();
        String customerSubSegment = request.getCustomerSubSegment();
        List<ThemeCategory> categories =
                themeCategoryRepository.findActiveCategories(currentDate, customerSegment, customerSubSegment);
        List<Long> categoryIds = new ArrayList<>();
        List<ThemeCategoryResponse> themeCategoryResponses = new ArrayList<>();
        categories.forEach(c -> {
            categoryIds.add(c.getId());
            themeCategoryResponses.add(themeMapper.toResponse(c));
        });
        List<ThemePreviewResponse> themePreviewResponses = categoryIds.isEmpty()
                ? List.of()
                : themeRepository.findActiveThemes(categoryIds, currentDate, customerSegment, customerSubSegment);
        return ThemePreviewListResponse.builder()
                .themePreviewResponses(themePreviewResponses)
                .themeCategoryResponses(themeCategoryResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public ThemeDownloadResponse downloadTheme(String themeCode) throws AppException {
        Theme theme = themeRepository
                .findByCodeWithPackages(themeCode)
                .orElseThrow(() -> new AppException("Theme not found with code " + themeCode));

        ThemePackage lightModeTheme = theme.getLightModeThemeId();
        ThemePackage darkModeTheme = theme.getDarkModeThemeId();

        return ThemeDownloadResponse.builder()
                .themeMetadata(buildThemeMetadata(theme))
                .lightMode(buildThemePackageData(lightModeTheme))
                .darkMode(buildThemePackageData(darkModeTheme))
                .build();
    }

    private ThemeDownloadResponse.ThemeMetadata buildThemeMetadata(Theme theme) {
        return ThemeDownloadResponse.ThemeMetadata.builder()
                .code(theme.getCode())
                .layout(theme.getLayout())
                .displayName(LanguageUtils.getLocalizedText(theme.getDisplayName()))
                .description(LanguageUtils.getLocalizedText(theme.getDescription()))
                .variant(theme.getVariant())
                .themeType(theme.getThemeType())
                .themeVersion(theme.getThemeVersion())
                .appVersion(theme.getAppVersion())
                .previewImageUrl(theme.getPreviewImageUrl())
                .animated(theme.getAnimated())
                .metadata(theme.getMetadata())
                .build();
    }

    private ThemeDownloadResponse.ThemePackageData buildThemePackageData(ThemePackage themePackage) {
        if (themePackage == null) {
            return null;
        }

        return ThemeDownloadResponse.ThemePackageData.builder()
                .backgroundType(themePackage.getBackgroundType())
                .backgroundUrl(themePackage.getBackgroundUrl())
                .textColor(themePackage.getTextColor())
                .textFontStyle(themePackage.getTextFontStyle())
                .secondaryColor(themePackage.getSecondaryColor())
                .secondaryBackgroundUrl(themePackage.getSecondaryBackgroundUrl())
                .secondaryBackgroundType(themePackage.getSecondaryBackgroundType())
                .mascot(buildMascotData(themePackage))
                .icons(buildIconsData(themePackage.getIcons()))
                .metadata(themePackage.getMetadata())
                .build();
    }

    private ThemeDownloadResponse.MascotData buildMascotData(ThemePackage themePackage) {
        return ThemeDownloadResponse.MascotData.builder()
                .imageUrl(themePackage.getMascotImageUrl())
                .secondaryImageUrl(themePackage.getSecondaryMascotImageUrl())
                .location(themePackage.getMascotLocation())
                .size(themePackage.getMascotSize())
                .color(themePackage.getMascotColor())
                .style(themePackage.getMascotStyle())
                .animation(ThemeDownloadResponse.AnimationData.builder()
                        .type(themePackage.getMascotAnimation())
                        .speed(themePackage.getMascotAnimationSpeed())
                        .direction(themePackage.getMascotAnimationDirection())
                        .repeat(themePackage.getMascotAnimationRepeat())
                        .build())
                .build();
    }

    private List<ThemeDownloadResponse.IconData> buildIconsData(List<ThemeIconSet> icons) {
        return icons.stream()
                .map(icon -> ThemeDownloadResponse.IconData.builder()
                        .code(icon.getCode())
                        .iconUrl(icon.getIconUrl())
                        .iconType(icon.getIconType())
                        .iconCategory(icon.getIconCategory())
                        .metadata(icon.getMetadata())
                        .build())
                .collect(Collectors.toList());
    }

    public AccentColorResponse createAccentColor(AccentColorCreateRequest request) {
        try {
            AccentColor accentColor = themeMapper.toEntity(request);
            accentColor.setStatus(StatusType.ACTIVE);
            accentColor = accentColorRepository.save(accentColor);
            AccentColorResponse response = themeMapper.toResponse(accentColor);
            response.setHexValue(response.getHexValue());
            return response;
        } catch (Exception e) {
            log.error("Error retrieving active accent colors", e);
            throw new BusinessException("Failed to retrieve active accent colors", e);
        }
    }

    public List<AccentColorResponse> getActiveAccentColors() {
        try {
            return accentColorRepository.findActiveColors().stream()
                    .map(accentColor -> {
                        AccentColorResponse response = themeMapper.toResponse(accentColor);
                        response.setHexValue(response.getHexValue());
                        return response;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving active accent colors", e);
            throw new BusinessException("Failed to retrieve active accent colors", e);
        }
    }

    @Transactional
    public ThemeResponse installTheme(String themeCode) {
        log.info("Installing theme with code: {}", themeCode);

        // Validate theme code
        themeValidator.validateThemeCode(themeCode);

        // Publish theme installation event
        eventPublisher.publishEvent(new ThemeInstalledEvent(this, "currentUserId", themeCode));
        return null;
    }

    public void validateThemeCode(String themeCode) {
        themeValidator.validateThemeCode(themeCode);
    }
}
