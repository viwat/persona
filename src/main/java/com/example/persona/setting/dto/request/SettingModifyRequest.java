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
public class SettingModifyRequest extends CustomerBaseRequest {
    private String settingKey;
    private String settingValue;
    private String updateReason;
    private Map<String, Object> metadata;
}
