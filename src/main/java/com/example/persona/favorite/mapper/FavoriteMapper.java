package com.example.persona.favorite.mapper;

import com.example.persona.favorite.dto.FavoriteCreateRequest;
import com.example.persona.favorite.dto.FavoriteResponse;
import com.example.persona.favorite.model.Favorite;
import com.example.persona.utils.ConfigValueUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @org.mapstruct.Builder(disableBuilder = true),
        uses = ConfigValueUtils.class)
public interface FavoriteMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(FavoriteCreateRequest request, @MappingTarget Favorite entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    // sets StatusType.ACTIVE
    Favorite toEntity(FavoriteCreateRequest request);

    FavoriteResponse toResponse(Favorite favoriteObj);
}
