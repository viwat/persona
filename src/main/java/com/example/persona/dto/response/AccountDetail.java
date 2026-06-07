package com.example.persona.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDetail {

    @JsonProperty("code")
    private String errorCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private AccountDetailData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountDetailData {

        @JsonProperty("category_profile_name")
        private String categoryProfileName;

        @JsonProperty("class_of_service_id")
        private String classOfServiceId;

        @JsonProperty("category_profile_id")
        private String categoryProfileId;

        @JsonProperty("category_id")
        private String categoryId;

        @JsonProperty("domain_id")
        private String domainId;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("no_debit")
        private boolean noDebit;

        @JsonProperty("no_credit")
        private boolean noCredit;

        @JsonProperty("no_frozen")
        private boolean frozen;

        @JsonProperty("no_dormant")
        private boolean dormant;

        @JsonProperty("no_blocked")
        private boolean blocked;

        @JsonProperty("current_balance")
        private BigDecimal currentBalance;

        @JsonProperty("actual_balance")
        private BigDecimal actualBalance;

        @JsonProperty("available_balance")
        private BigDecimal availableBalance;

        @JsonProperty("full_name")
        private String name;

        @JsonProperty("account_class")
        private String accountClass;

        @JsonProperty("entity_code")
        private String entityCode;

        @JsonProperty("account_no")
        private String accountNo;

        @JsonProperty("customer_no")
        private String customerNo;

        @JsonProperty("ccy")
        private String ccy;

        @JsonProperty("branch_code")
        private String branchCode;

        @JsonProperty("local_branch")
        private String localBranch;

        @JsonProperty("party_id")
        private String partyId;

        @JsonProperty("msisdn")
        private String msisdn;

        @JsonProperty("sex")
        private String sex;

        @JsonProperty("nationality")
        private String nationality;

        @JsonProperty("date_of_birth")
        private String dataOfBirth;

        @JsonProperty("mobile_number")
        private String mobileNumber;

        @JsonProperty("telephone")
        private String telPhone;

        @JsonProperty("e_mail")
        private String email;

        @JsonProperty("company_name")
        private String companyName;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        @JsonProperty("account_type")
        private String accountType;

        @JsonProperty("customer_type")
        private String customerType;

        @JsonProperty("customer_category")
        private String customerCategory;

        @JsonProperty("customer_prefix")
        private String customerPrefix;

        @JsonProperty("customer_name1")
        private String customerName1;

        @JsonProperty("middle_name")
        private String middleName;

        @JsonProperty("first_name_in_khmer")
        private String firstNameInKhmer;

        @JsonProperty("last_name_in_khmer")
        private String lastNameInKhmer;

        @JsonProperty("resident_status")
        private String residentStatus;

        @JsonProperty("kyc_status")
        private String kycStatus;

        @JsonProperty("p_national_id")
        private String pNationalId;

        @JsonProperty("passport_no")
        private String passportNo;

        @JsonProperty("ppt_iss_date")
        private String pptIssDate;

        @JsonProperty("ppt_exp_date")
        private String pptExpDate;

        @JsonProperty("birth_country")
        private String birthCountry;

        @JsonProperty("place_of_birth")
        private String placeOfBirth;

        @JsonProperty("unique_id_name")
        private String uniqueIdName;

        @JsonProperty("unique_id_value")
        private String uniqueIdValue;

        @JsonProperty("country_of_issuance")
        private String countryOfIssuance;

        @JsonProperty("unique_id_exp")
        private String uniqueIdExp;

        @JsonProperty("marital_status")
        private String maritalStatus;

        @JsonProperty("street_house")
        private String streetHouse;

        @JsonProperty("address_line1")
        private String addressLine1;

        @JsonProperty("address_line2")
        private String addressLine2;

        @JsonProperty("address_line3")
        private String addressLine3;

        @JsonProperty("address_line4")
        private String addressLine4;

        @JsonProperty("address")
        private String address;

        @JsonProperty("pincode")
        private String pincode;

        @JsonProperty("source_of_income")
        private String sourceOfIncome;

        @JsonProperty("monthly_income")
        private String monthlyIncome;

        @JsonProperty("industry_sector")
        private String industrySector;

        @JsonProperty("occupation")
        private String occupation;

        @JsonProperty("company_address")
        private String companyAddress;

        @JsonProperty("job_length")
        private String jobLength;

        @JsonProperty("us_tin")
        private String usTin;

        @JsonProperty("aml_check")
        private String amlCheck;

        @JsonProperty("aml_verification")
        private String amlVerification;

        @JsonProperty("aml_risk_level")
        private String amlRiskLevel;

        @JsonProperty("aml_remark")
        private String amlRemark;

        @JsonProperty("record_stat")
        private String recordStat;

        @JsonProperty("auth_stat")
        private String authStat;

        @JsonProperty("account_created_on")
        private String accountCreatedOn;

        @JsonProperty("province_of_birth")
        private String provinceOfBirth;

        @JsonProperty("birth_pincode")
        private String birthPincode;

        @JsonProperty("p_address1")
        private String pAddress1;

        @JsonProperty("p_address2")
        private String pAddress2;

        @JsonProperty("p_address3")
        private String pAddress3;

        @JsonProperty("p_address4")
        private String pAddress4;

        @JsonProperty("p_address")
        private String pAddress;

        @JsonProperty("p_pincode")
        private String pPincode;

        @JsonProperty("p_country")
        private String pCountry;

        @JsonProperty("d_address1")
        private String dAddress1;

        @JsonProperty("d_address2")
        private String dAddress2;

        @JsonProperty("d_address3")
        private String dAddress3;

        @JsonProperty("d_address4")
        private String dAddress4;

        @JsonProperty("d_address")
        private String dAddress;

        @JsonProperty("d_pincode")
        private String dPincode;

        @JsonProperty("d_country")
        private String dCountry;

        @JsonProperty("country")
        private String country;

        @JsonProperty("banking_purpose")
        private String bankingPurpose;

        @JsonProperty("register_by")
        private String registerBy;

        @JsonProperty("register_name")
        private String registerName;

        @JsonProperty("promo_code")
        private String promoCode;

        @JsonProperty("employment_id")
        private String employmentId;

        @JsonProperty("is_retailer")
        private Boolean retailer;

        @JsonProperty("cashout_package")
        private String cashoutPackage;

        @JsonProperty("subscription_package")
        private String subscriptionPackage;

        @JsonProperty("referral_code")
        private String referralCode;
    }
}
