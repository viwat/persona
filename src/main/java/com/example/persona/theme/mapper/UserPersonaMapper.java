package com.example.persona.theme.mapper;

import com.example.persona.dto.response.CustomerPersonaResponse;
import com.example.persona.model.CustomerPersona;
import com.example.persona.theme.dto.request.UserPersonaCreateRequest;
import com.example.persona.theme.dto.response.UserPersonaResponse;
import com.example.persona.theme.model.UserPersona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPersonaMapper {
    UserPersona toEntity(UserPersonaCreateRequest request);

    UserPersonaResponse toResponse(UserPersona userPersona);

    @Mappings({
        @Mapping(target = "themeCode", source = "persona.themeCode"),
        @Mapping(target = "accentColor", source = "persona.accentColor"),
        @Mapping(target = "textSize", source = "persona.textSize"),
        @Mapping(target = "appearance", source = "persona.appearance"),
    })
    CustomerPersonaResponse toCustomerPersonaResponse(CustomerPersona userPersona);

    CustomerPersonaResponse toCustomerPersonaResponse(UserPersona userPersona);

    CustomerPersona toCustomerPersona(UserPersona userPersona);
}
