package com.example.persona.setting.mapper;

import com.example.persona.setting.dto.request.UserSettingCreateRequest;
import com.example.persona.setting.dto.response.UserSettingResponse;
import com.example.persona.setting.model.UserSetting;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface UserSettingMapper {

    // @BeanMapping(nullValuePropertyMappingStrategy =
    // NullValuePropertyMappingStrategy.IGNORE)
    // void updateEntityFromRequest(UserSettingMRequest request, @MappingTarget
    // UserSetting entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "setting", ignore = true)
    UserSetting toEntity(UserSettingCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toEntity(UserSettingCreateRequest request, @MappingTarget UserSetting userSetting);

    @Mapping(target = "settingId", source = "setting.id")
    @Mapping(target = "settingCode", source = "setting.code")
    @Mapping(target = "settingKey", source = "setting.settingKey")
    UserSettingResponse toResponse(UserSetting entity);
}
