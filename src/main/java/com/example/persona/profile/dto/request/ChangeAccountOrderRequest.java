package com.example.persona.profile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.dto.CustomerBaseRequest;
import jakarta.validation.constraints.NotNull;
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
public class ChangeAccountOrderRequest extends CustomerBaseRequest {

    @JsonProperty("display_order")
    @NotNull
    private Integer displayOrder;
}
