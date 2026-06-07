package com.example.persona.favorite.mapper;

import com.example.persona.favorite.dto.UserFavoriteCreateRequest;
import com.example.persona.favorite.dto.UserFavoriteResponse;
import com.example.persona.favorite.model.UserFavorite;
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
public interface UserFavoriteMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UserFavoriteCreateRequest request, @MappingTarget UserFavorite entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    // sets StatusType.ACTIVE
    UserFavorite toEntity(UserFavoriteCreateRequest request);

    UserFavoriteResponse toResponse(UserFavorite favoriteObj);
}
