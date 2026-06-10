package com.example.persona.profile.dto.request;

import com.example.persona.dto.CustomerBaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RenameAccountRequest extends CustomerBaseRequest {

    @JsonProperty("account_name")
    @NotBlank
    private String accountName;
}
