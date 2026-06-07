package com.example.persona.misc.mapper;

import com.example.persona.misc.dto.request.EnumerationCreateRequest;
import com.example.persona.misc.dto.request.EnumerationModifyRequest;
import com.example.persona.misc.dto.response.EnumerationResponse;
import com.example.persona.misc.model.Enumeration;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EnumerationMapper {

    // Create
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    Enumeration toEntity(EnumerationCreateRequest request);

    // Modify (update existing entity)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(EnumerationModifyRequest request, @MappingTarget Enumeration entity);

    // To Response
    EnumerationResponse toResponse(Enumeration entity);
}
