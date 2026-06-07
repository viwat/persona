package com.example.persona.home.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateBannerResponse {
    private Boolean updateAvailable;
    private String currentVersion;
    private String latestVersion;
    private Boolean forceUpdate;
    private String updateMessage;
    private String updateUrl;
}
