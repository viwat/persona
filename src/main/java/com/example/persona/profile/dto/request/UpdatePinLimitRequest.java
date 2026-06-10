package com.example.persona.profile.dto.request;

import com.example.persona.dto.CustomerBaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
public class UpdatePinLimitRequest extends CustomerBaseRequest {

    @JsonProperty("pin_limit_amount")
    @NotNull
    private BigDecimal pinLimitAmount;
}
