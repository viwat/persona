package com.example.persona.profile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.dto.CustomerBaseRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class CustomerAppProfileRequest extends CustomerBaseRequest {

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

    @JsonProperty("kyc_status")
    private String kycStatus;

    @JsonProperty("pin_limit_amount")
    private BigDecimal pinLimitAmount;

    @JsonProperty("profile_identification_expired")
    private LocalDateTime profileIdentificationExpired;

    @JsonProperty("device_id")
    private String deviceId;
}
