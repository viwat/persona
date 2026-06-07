package com.example.persona.setting.dto.response;

import java.util.Map;
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
public class UserSettingResponse {
    private Long id;

    /* ---- Setting ---- */
    private Long settingId;
    private String settingCode; // optional but very useful for frontend
    private String settingKey; // optional display field

    /* ---- Value ---- */
    private String settingValue;
    private Map<String, Object> metadata;

    /* ---- Customer Info ---- */
    private String customerKey;
    private String customerNo;
    private String accountNo;
    private String phoneNo;
    private String masterAccountNo;
    private String channelCode;
    private String variant;
    private String code;
    private String deviceId;
}
