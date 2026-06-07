package com.example.persona.profile.model;

import com.example.persona.model.BaseCustomerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dgtl_customer_app_profile")
public class CustomerAppProfile extends BaseCustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_app_id", nullable = false)
    private String customerAppId;

    @Column(name = "pin_limit_amount", precision = 19, scale = 2)
    private BigDecimal pinLimitAmount;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_name_kh")
    private String customerNameKh;

    @Column(name = "gender")
    private String gender;

    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "email")
    private String email;

    @Column(name = "current_address", length = 500)
    private String currentAddress;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "profile_identification_expired")
    private LocalDateTime profileIdentificationExpired;

    @Column(name = "kyc_status")
    private String kycStatus;

    @Column(name = "kyc_status_date")
    private LocalDate kycStatusDate;

    @Column(name = "kyc_status_reason")
    private String kycStatusReason;

    @Column(name = "show_account_list_by_cif")
    private Boolean showAccountListByCif; // true = show account list based on CIF no, false = show based on customer_no

    @Column(name = "meta_data", length = 2000)
    private String metaData;

    @Column(name = "version_key", nullable = false)
    private String versionKey; // Groups versions: customer_key or customer_no

    @Column(name = "previous_version_id")
    private Long previousVersionId; // Links to previous version

    @Column(name = "checksum_sha256")
    private String checksumSha256;
}
