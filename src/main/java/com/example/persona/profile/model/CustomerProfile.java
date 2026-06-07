package com.example.persona.profile.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dgtl_customer_profile")
public class CustomerProfile extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_no")
    private String customerNo;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_type")
    private String customerType;

    @Column(name = "customer_status")
    private String customerStatus;

    @Column(name = "customer_segment")
    private String customerSegment;

    @Column(name = "customer_group")
    private String customerGroup;

    @Column(name = "customer_class")
    private String customerClass;

    @Column(name = "customer_sub_class")
    private String customerSubClass;

    @Column(name = "customer_category")
    private String customerCategory;

    @Column(name = "customer_sub_category")
    private String customerSubCategory;

    @Column(name = "kyc_status")
    private String kycStatus;

    @Column(name = "kyc_status_date")
    private LocalDate kycStatusDate;

    @Column(name = "kyc_status_reason")
    private String kycStatusReason;

    @Column(name = "meta_data")
    private String metaData;
}
