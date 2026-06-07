package com.example.persona.theme.dto.request;

import com.example.persona.dto.CustomerBaseRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserPersonaCreateRequest extends CustomerBaseRequest {
    private String personaCode;
    private String channelCode;
    private String themeCode;
    private String themeCategory;
    private String accentColor;
    private String appIcon;
    private String appearance;
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
