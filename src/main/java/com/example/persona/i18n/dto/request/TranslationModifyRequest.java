package com.example.persona.i18n.dto.request;

import com.example.persona.enums.StatusType;
import com.example.persona.model.MultilingualContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TranslationModifyRequest {
    private String code;
    private String appCode;
    private String category;
    private String subCategory;
    private String type;

    private MultilingualContent name;

    private String description;
    private String metadata;

    private StatusType status;
}
