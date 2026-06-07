package com.example.persona.profile.dto.request;

import com.example.persona.dto.CustomerBaseRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class AccountProfileRequest extends CustomerBaseRequest {
    private String serviceType;
    private String entryType;
}
