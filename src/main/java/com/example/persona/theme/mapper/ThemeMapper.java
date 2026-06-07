package com.example.persona.theme.mapper;

import com.example.persona.theme.dto.request.AccentColorCreateRequest;
import com.example.persona.theme.dto.request.ThemeCategoryCreateRequest;
import com.example.persona.theme.dto.request.ThemeCategoryModifyRequest;
import com.example.persona.theme.dto.request.ThemeCreateRequest;
import com.example.persona.theme.dto.request.ThemeIconSetCreateRequest;
import com.example.persona.theme.dto.request.ThemeIconSetModifyRequest;
import com.example.persona.theme.dto.request.ThemePackageCreateRequest;
import com.example.persona.theme.dto.request.ThemePackageModifyRequest;
import com.example.persona.theme.dto.response.AccentColorResponse;
import com.example.persona.theme.dto.response.ThemeCategoryResponse;
import com.example.persona.theme.dto.response.ThemeIconSetResponse;
import com.example.persona.theme.dto.response.ThemePackageResponse;
import com.example.persona.theme.dto.response.ThemeResponse;
import com.example.persona.theme.model.AccentColor;
import com.example.persona.theme.model.Theme;
import com.example.persona.theme.model.ThemeCategory;
import com.example.persona.theme.model.ThemeIconSet;
import com.example.persona.theme.model.ThemePackage;
import com.example.persona.utils.LanguageUtils;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = LanguageUtils.class)
public interface ThemeMapper {
    @Mapping(target = "displayName", expression = "java(LanguageUtils.getLocalizedText(theme.getDisplayName()))")
    @Mapping(target = "description", expression = "java(LanguageUtils.getLocalizedText(theme.getDescription()))")
    ThemeResponse toResponse(Theme theme);

    @Mapping(target = "displayName", expression = "java(LanguageUtils.getLocalizedText(theme.getDisplayName()))")
    @Mapping(target = "description", expression = "java(LanguageUtils.getLocalizedText(theme.getDescription()))")
    List<ThemeResponse> toThemeResponses(List<Theme> themes);

    Theme toEntity(ThemeCreateRequest request);

    @Mapping(target = "name", expression = "java(LanguageUtils.getLocalizedText(category.getName()))")
    @Mapping(target = "description", expression = "java(LanguageUtils.getLocalizedText(category.getDescription()))")
    ThemeCategoryResponse toResponse(ThemeCategory category);

    ThemeCategory toEntity(ThemeCategoryCreateRequest request);

    void updateEntityFromRequest(ThemeCategoryModifyRequest request, @MappingTarget ThemeCategory category);

    @Mapping(target = "icons", ignore = true)
    void updateEntityFromRequest(ThemePackageModifyRequest request, @MappingTarget ThemePackage themePackage);

    AccentColor toEntity(AccentColorCreateRequest request);

    ThemePackage toEntity(ThemePackageCreateRequest request);

    ThemePackage toEntity(ThemePackageModifyRequest request);

    ThemePackageResponse toResponse(ThemePackage themePackage);

    ThemeIconSet toEntity(ThemeIconSetCreateRequest request);

    List<ThemeIconSet> toEntity(List<ThemeIconSetModifyRequest> request);

    ThemeIconSetResponse toResponse(ThemeIconSet iconSet);

    // Map a List of ThemeIconSet to a List of ThemeIconSetResponse
    List<ThemeIconSetResponse> toResponse(List<ThemeIconSet> iconSets);

    @Mapping(target = "displayName", expression = "java(LanguageUtils.getLocalizedText(accentColor.getDisplayName()))")
    AccentColorResponse toResponse(AccentColor accentColor);
}
