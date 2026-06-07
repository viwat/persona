package com.example.persona.setting.dto.request;

import com.example.persona.dto.CustomerBaseRequest;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SettingCreateRequest extends CustomerBaseRequest {
    private String settingType;
    private String description;
    private String code;
    private String settingKey;
    private String defaultValue;
    private String category;
    private String permission;
    private Integer displayOrder;
    private Map<String, Object> metadata;
    private String callbackUrl;
}
