package com.example.persona.favorite.dto;

import com.example.persona.enums.StatusType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FavoriteFilterRequest {

    private String serviceType;
    private String serviceCode;
    private String keyword;
    private String appVersion;
    private String context;

    private StatusType status = StatusType.ACTIVE;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}
