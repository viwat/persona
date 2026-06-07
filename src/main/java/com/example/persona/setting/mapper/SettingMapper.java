package com.example.persona.setting.mapper;

import com.example.persona.setting.dto.request.SettingCreateRequest;
import com.example.persona.setting.dto.response.SettingResponse;
import com.example.persona.setting.model.Setting;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @org.mapstruct.Builder(disableBuilder = true))
public interface SettingMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(SettingCreateRequest request, @MappingTarget Setting entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    // sets StatusType.ACTIVE
    Setting toEntity(SettingCreateRequest request);

    @Mapping(target = "settingValue", source = "defaultValue")
    SettingResponse toResponse(Setting setting);
}
