package com.example.persona.schedule.mapper;

import com.example.persona.schedule.dto.UserScheduleCreateRequest;
import com.example.persona.schedule.dto.UserScheduleModifyRequest;
import com.example.persona.schedule.model.UserSchedule;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserScheduleMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UserScheduleModifyRequest request, @MappingTarget UserSchedule entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
        @Mapping(source = "request.configValue1", target = "configValue1"),
        @Mapping(source = "request.configValue2", target = "configValue2"),
        @Mapping(source = "request.configValue3", target = "configValue3"),
        @Mapping(source = "request.configValue4", target = "configValue4"),
        @Mapping(source = "request.configValue5", target = "configValue5"),
        @Mapping(source = "request.configValue6", target = "configValue6"),
        @Mapping(source = "request.configValue7", target = "configValue7"),
        @Mapping(source = "request.configValue8", target = "configValue8"),
        @Mapping(source = "request.configValue9", target = "configValue9"),
        @Mapping(source = "request.configValue10", target = "configValue10")
    })
    void toEntity(UserScheduleCreateRequest request, @MappingTarget UserSchedule userSchedule);
}
