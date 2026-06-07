package com.example.persona.setting.mapper;

import com.example.persona.setting.dto.response.UserSettingHistoryResponse;
import com.example.persona.setting.model.UserSettingHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserSettingHistoryMapper {

    @Mapping(source = "userSetting.id", target = "userSettingId")
    UserSettingHistoryResponse toResponse(UserSettingHistory entity);
}
