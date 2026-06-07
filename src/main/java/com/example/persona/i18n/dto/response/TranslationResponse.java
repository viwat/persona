package com.example.persona.i18n.dto.response;

import com.example.persona.enums.StatusType;
import com.example.persona.model.MultilingualContent;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TranslationResponse {
    private Long id;

    private String code;
    private String appCode;
    private String category;
    private String subCategory;
    private String type;

    private String translation;
    private MultilingualContent name;

    private String description;
    private String metadata;

    private StatusType status;

    private Integer version;

    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
}
