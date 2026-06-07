package com.example.persona.profile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.dto.CustomerBaseRequest;
import jakarta.validation.constraints.NotBlank;
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
public class ChangeCardOrderRequest extends CustomerBaseRequest {

    @JsonProperty("account_no")
    @NotBlank
    private String accountNo;

    @JsonProperty("card_no")
    @NotBlank
    private String cardNo;

    @JsonProperty("display_order")
    @NotNull
    private Integer displayOrder;
}
