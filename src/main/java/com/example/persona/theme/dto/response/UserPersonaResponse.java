package com.example.persona.theme.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserPersonaResponse {
    private Long id;
    private String code;
    private String personaCode;
    private String channelCode;

    private String customerKey;
    private String masterAccountNo;

    private String themeCode;
    private String themeCategory;
    private String accentColor;
    private String appearance;
    private String appIcon;
    private String textSize;
    private String metadata;
    private String controlAttr1;
    private String controlAttr2;
    private String controlAttr3;
    private String controlAttr4;
    private String controlAttr5;
    private List<String> cardOrders;
    private List<String> serviceOrders;
    private List<String> accountOrders;
    private List<String> widgetOrders;
    private List<String> suggestedWidgets;
}
