package com.example.persona.profile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AccountHolderResponse {
    @JsonProperty("holder_name")
    private String holderName;

    @JsonProperty("holder_type")
    private String holderType;

    @JsonProperty("relationship_type")
    private String relationshipType;

    @JsonProperty("holder_customer_no")
    private String holderCustomerNo;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("meta_data")
    private String metaData;
}
