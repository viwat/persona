package com.example.persona.i18n.mapper;

import com.example.persona.i18n.dto.request.TranslationCreateRequest;
import com.example.persona.i18n.dto.request.TranslationModifyRequest;
import com.example.persona.i18n.dto.response.TranslationResponse;
import com.example.persona.i18n.model.Translation;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TranslationMapper {

    /*
     * ========================= Entity -> Response =========================
     */
    @Mapping(target = "translation", expression = "java(getTranslationByLanguage(entity, language))")
    TranslationResponse toResponse(Translation entity, @Context String language);

    /*
     * ========================= Create Request -> Entity =========================
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Translation toEntity(TranslationCreateRequest request);

    /*
     * ========================= Update Existing Entity =========================
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromRequest(TranslationModifyRequest request, @MappingTarget Translation entity);

    /*
     * ========================= Custom Language Resolver =========================
     */
    default String getTranslationByLanguage(Translation translation, String language) {

        if (translation == null || translation.getName() == null || language == null) {
            return null;
        }

        return switch (language.toLowerCase()) {
            case "en" -> translation.getName().getEn();
            case "km" -> translation.getName().getKm();
            case "zh" -> translation.getName().getZh();
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }
}
