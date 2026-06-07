package com.example.persona.pinless.mapper;

import com.example.persona.pinless.model.dto.PinlessConfigData;
import com.example.persona.pinless.model.dto.PinlessConfigHistoryResponse;
import com.example.persona.pinless.model.dto.PinlessConfigRequest;
import com.example.persona.pinless.model.dto.PinlessConfigResponse;
import com.example.persona.pinless.model.entity.PinlessConfig;
import com.example.persona.pinless.model.entity.PinlessConfigHistory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PinlessConfigMapper {

    @Mapping(target = "systemCap", ignore = true)
    PinlessConfigData toData(PinlessConfig entity);

    PinlessConfigResponse toResponse(PinlessConfigData data);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PinlessConfigRequest request, @MappingTarget PinlessConfig entity);

    PinlessConfigHistoryResponse toHistoryResponse(PinlessConfigHistory entity);
}
