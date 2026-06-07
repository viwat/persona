package com.example.persona.widget.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WidgetResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("is_default")
    private Boolean isDefault;

    @JsonProperty("description")
    private String description;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("deep_link")
    private String deepLink;

    @JsonProperty("secondary_preview_url")
    private String secondaryPreviewUrl;

    @JsonProperty("app_version")
    private String appVersion;

    @JsonProperty("param1_key")
    private String param1Key;

    @JsonProperty("param1_type")
    private String param1Type;

    @JsonProperty("param2_key")
    private String param2Key;

    @JsonProperty("param2_type")
    private String param2Type;

    @JsonProperty("param3_key")
    private String param3Key;

    @JsonProperty("param3_type")
    private String param3Type;

    @JsonProperty("param4_key")
    private String param4Key;

    @JsonProperty("param4_type")
    private String param4Type;

    @JsonProperty("param5_key")
    private String param5Key;

    @JsonProperty("param5_type")
    private String param5Type;
}
