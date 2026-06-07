package com.example.persona.migration.model.oracle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MTX_USER_PHYSICAL_CARD")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MtxUserPhysicalCard {

    @Id
    @Column(name = "TRACKING_NUMBER", length = 20, nullable = false)
    private String trackingNumber;

    @Column(name = "MASTER_ACCOUNT_ID", length = 20, nullable = false)
    private String masterAccountId;

    @Column(name = "PARTY_ID", length = 20)
    private String partyId;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CARD_TYPE", length = 10)
    private String cardType;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "EMAIL", length = 50)
    private String email;

    @Column(name = "CARD_STATUS", length = 2)
    private String cardStatus;

    @Column(name = "MODIFIED_ON")
    private LocalDateTime modifiedOn;

    @Column(name = "MODIFIED_BY", length = 20)
    private String modifiedBy;

    @Column(name = "FIRST_SET_PIN_ON")
    private LocalDateTime firstSetPinOn;

    @Column(name = "PHONE_NUMBER", length = 20)
    private String phoneNumber;

    @Column(name = "CLASS_OF_SERVICE_ID", length = 20)
    private String classOfServiceId;

    @Column(name = "CARD_NUMBER", length = 20)
    private String cardNumber;

    @Column(name = "LAST_PIN_MODIFIED_ON")
    private LocalDateTime lastPinModifiedOn;

    @Column(name = "CATEGORY_PROFILE_ID", length = 20)
    private String categoryProfileId;

    @Column(name = "SC_ENABLED", length = 1)
    private String scEnabled;

    @Column(name = "SECURE_CODE", length = 100)
    private String secureCode;

    @Column(name = "EXPIRY_DATE", length = 10)
    private String expiryDate;

    @Column(name = "ACCOUNT_NO", length = 20)
    private String accountNo;

    @Column(name = "MAX_DLY_PUR_AMT", precision = 25)
    private BigDecimal maxDlyPurAmt;

    @Column(name = "DLY_PUR_LIM", precision = 25)
    private BigDecimal dlyPurLim;

    @Column(name = "ENTITY_CODE", length = 20)
    private String entityCode;

    @Column(name = "SYSTEM_MESSAGE")
    private String systemMessage;
}
