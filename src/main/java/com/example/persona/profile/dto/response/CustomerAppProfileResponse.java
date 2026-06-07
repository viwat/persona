package com.example.persona.profile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class CustomerAppProfileResponse {

    @JsonProperty("customer_no")
    private String customerNo;

    @JsonProperty("phone_no")
    private String phoneNo;

    @JsonProperty("kyc_status")
    private String kycStatus;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("customer_name_kh")
    private String customerNameKh;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("marital_status")
    private String maritalStatus;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @JsonProperty("place_of_birth")
    private String placeOfBirth;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("email")
    private String email;

    @JsonProperty("current_address")
    private String currentAddress;

    @JsonProperty("id_type")
    private String idType;

    @JsonProperty("id_number")
    private String idNumber;

    @JsonProperty("profile_identification_expired")
    private LocalDateTime profileIdentificationExpired;

    @JsonProperty("pin_limit_amount")
    private BigDecimal pinLimitAmount;

    @JsonProperty("meta_data")
    private String metaData;
}
