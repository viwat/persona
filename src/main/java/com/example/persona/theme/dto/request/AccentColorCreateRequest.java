package com.example.persona.theme.dto.request;

import com.example.persona.model.MultilingualContent;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccentColorCreateRequest {

    private MultilingualContent displayName;

    private String colorCode;
    private String rgbCode;
    private String iconUrl;
}
