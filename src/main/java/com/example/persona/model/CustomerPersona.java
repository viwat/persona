package com.example.persona.model;

import com.example.persona.dashboard.AccountDashboardResponse;
import com.example.persona.profile.dto.response.UserProfileResponse;
import com.example.persona.theme.dto.response.UserPersonaResponse;
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
public class CustomerPersona {
    private String customerNo;
    private String accountNo;
    private String phoneNo;
    private String masterAccountNo;
    private String customerKey;
    private String channelCode;

    private UserPersonaResponse persona;
    private UserProfileResponse profile;
    private AccountDashboardResponse dashboard;
}
