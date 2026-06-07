package com.example.persona.favorite.mapper;

import com.example.persona.favorite.model.UserFavorite;
import com.example.persona.favorite.model.UserFavoriteHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserFavoriteHistoryMapper {

    @Mappings({
        // do not copy PK
        @Mapping(target = "id", ignore = true),

        // relationship
        @Mapping(target = "userFavorite", source = "userFavorite"),
        @Mapping(target = "userFavoriteId", source = "id"),
        @Mapping(target = "isPinned", source = "pinned"),

        // status if you still have it in BaseCustomerEntity
        // @Mapping(target = "status", expression = "java(StatusType.ACTIVE)")
    })
    UserFavoriteHistory fromUserFavorite(UserFavorite userFavorite);
}
