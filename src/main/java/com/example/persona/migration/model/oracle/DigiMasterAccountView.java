package com.example.persona.migration.model.oracle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing the Oracle view DIGI_MASTER_ACCOUNT_V. This view combines
 * data from PORTAL_MASTER_BINDING and PORTAL_MASTER_ACCOUNT tables. Note: This
 * is a read-only entity mapped to a database view.
 */
@Entity
@Table(name = "DIGI_MASTER_ACCOUNT_V")
@Getter
@Setter
public class DigiMasterAccountView {

    @Id
    @Column(name = "MASTER_ACC_ID")
    private String masterAccId;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "PARTY_ID")
    private String partyId;

    @Column(name = "LOGIN_ID")
    private String loginId;

    @Column(name = "ENTITY")
    private String entity;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "APPLICATION_ID")
    private String applicationId;

    @Column(name = "PARENT_MASTER_ID")
    private String parentMasterId;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;
}
