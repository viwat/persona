package com.example.persona.schedule.mapper;

import com.example.persona.schedule.dto.ScheduleCreateRequest;
import com.example.persona.schedule.dto.ScheduleModifyRequest;
import com.example.persona.schedule.dto.response.ScheduleResponse;
import com.example.persona.schedule.model.Schedule;
import java.util.List;
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
public interface ScheduleMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ScheduleModifyRequest request, @MappingTarget Schedule entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    // sets StatusType.ACTIVE
    Schedule toEntity(ScheduleCreateRequest request);

    ScheduleResponse toResponse(Schedule schedule);

    List<ScheduleResponse> toResponse(List<Schedule> schedule);
}
