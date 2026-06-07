package com.example.persona.setting.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingResponse implements Serializable {
    @JsonProperty("setting_key")
    private String settingKey;

    @JsonProperty("setting_value")
    private String settingValue;

    @JsonProperty("setting_type")
    private String settingType;

    @JsonProperty("category")
    private String category;

    @JsonProperty("description")
    private String description;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_date")
    private LocalDateTime createdDate;

    @JsonProperty("modified_by")
    private String modifiedBy;

    @JsonProperty("modified_date")
    private LocalDateTime modifiedDate;
}
