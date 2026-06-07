package com.example.persona.setting.dto.response;

import java.time.LocalDateTime;
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
public class UserSettingHistoryResponse {
    private Long id;
    private Long userSettingId;
    private String settingKey;
    private String oldSettingValue;
    private String newSettingValue;
    private String updateReason;
    private Map<String, Object> metadata;

    private String customerKey;
    private String customerNo;
    private String accountNo;
    private String phoneNo;
    private String masterAccountNo;
    private String channelCode;
    private String variant;
    private String code;
    private String deviceId;

    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
    private String status;
    private Integer version;
}
