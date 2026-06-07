package com.example.persona.setting.dto.request;

import com.example.persona.dto.CustomerBaseRequest;
import java.util.Map;
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
public class UserSettingCreateRequest extends CustomerBaseRequest {
    private String settingKey;
    private String settingValue;
    private Map<String, Object> metadata;
}
