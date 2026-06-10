package com.example.persona.profile.dto.request;

import com.example.persona.dto.CustomerBaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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
public class UpdateCardTransactionLimitRequest extends CustomerBaseRequest {

    @JsonProperty("account_no")
    @NotBlank
    private String accountNo;

    @JsonProperty("card_no")
    @NotBlank
    private String cardNo;

    @JsonProperty("transaction_max_limit")
    @NotNull
    private BigDecimal transactionMaxLimit;
}
