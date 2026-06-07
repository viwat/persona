package com.example.persona.migration.seeder;

import com.example.persona.migration.model.oracle.DigiMasterAccountView;
import com.example.persona.migration.model.oracle.MtxUserPhysicalCard;
import com.example.persona.migration.model.oracle.MtxVirtualCard;
import com.example.persona.migration.model.oracle.PortalMasterDevice;
import com.example.persona.migration.repository.oracle.DigiMasterAccountViewRepository;
import com.example.persona.migration.repository.oracle.MtxUserPhysicalCardRepository;
import com.example.persona.migration.repository.oracle.MtxVirtualCardRepository;
import com.example.persona.migration.repository.oracle.PortalMasterDeviceRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Populates the H2 in-memory Oracle-simulation datasource with sample records
 * so that all migration handlers can be exercised locally.
 *
 * <p><strong>Sample customer key:</strong> {@code 100000001_ACC001_MACC001}
 * <ul>
 *   <li>customerNo = {@code 100000001}
 *   <li>accountNo  = {@code ACC001}
 *   <li>masterAccId = {@code MACC001}
 * </ul>
 *
 * <p>Trigger a full migration via:
 * <pre>POST /migration/submit/100000001_ACC001_MACC001</pre>
 */
@Slf4j
@Component
@Profile("local")
public class LocalOracleDataSeeder {

    private final DigiMasterAccountViewRepository digiRepo;
    private final PortalMasterDeviceRepository deviceRepo;
    private final MtxVirtualCardRepository virtualCardRepo;
    private final MtxUserPhysicalCardRepository physicalCardRepo;

    // Constructor injection with qualifier-aware wiring via @Autowired field is
    // not needed here — Spring routes these repos to oracleEntityManagerFactory
    // automatically because LocalOracleConfig.@EnableJpaRepositories covers their package.
    @Autowired
    public LocalOracleDataSeeder(
            DigiMasterAccountViewRepository digiRepo,
            PortalMasterDeviceRepository deviceRepo,
            MtxVirtualCardRepository virtualCardRepo,
            MtxUserPhysicalCardRepository physicalCardRepo) {
        this.digiRepo = digiRepo;
        this.deviceRepo = deviceRepo;
        this.virtualCardRepo = virtualCardRepo;
        this.physicalCardRepo = physicalCardRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional("oracleTransactionManager")
    public void seed() {
        if (digiRepo.count() > 0) {
            log.debug("[LocalOracleDataSeeder] Oracle source data already present — skipping");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // ── Master account view (Oracle DIGI_MASTER_ACCOUNT_V) ─────────────
        // loginId = accountNo so the handler finds it via findByLoginIdAndApplicationId
        DigiMasterAccountView masterAcc = new DigiMasterAccountView();
        masterAcc.setMasterAccId("MACC001");
        masterAcc.setUserName("testuser");
        masterAcc.setPartyId("100000001");
        masterAcc.setLoginId("ACC001");            // accountNo used as loginId in WINGPAY
        masterAcc.setEntity("WING");
        masterAcc.setPhoneNumber("0981234567");
        masterAcc.setStatus("A");
        masterAcc.setApplicationId("WINGPAY");
        masterAcc.setCreatedOn(now);
        digiRepo.save(masterAcc);

        // ── Device (Oracle PORTAL_MASTER_DEVICES_ID) ───────────────────────
        PortalMasterDevice device = PortalMasterDevice.builder()
                .masterDeviceId("DEV001")
                .masterAccId("MACC001")
                .deviceId("TEST-DEVICE-UUID-001")
                .status("DA")                      // "DA" = Device Active
                .osPlatform("ANDROID")
                .osVersion("13")
                .createdOn(now)
                .modifiedOn(now)
                .build();
        deviceRepo.save(device);

        // ── Virtual card (Oracle MTX_VIRTUAL_CARD) ─────────────────────────
        MtxVirtualCard virtualCard = MtxVirtualCard.builder()
                .masterAccId("MACC001")
                .trackingNumber("VCARD001")        // composite PK part
                .cardType("VISA")                  // composite PK part
                .accountNo("ACC001")
                .cardStatus("A")
                .cardNumber("4567")
                .email("test@example.com")
                .phoneNumber("0981234567")
                .expiryDate("12/27")
                .maxDlyPurAmt(BigDecimal.valueOf(1000.00))
                .dlyPurLim(BigDecimal.valueOf(500.00))
                .createdOn(now)
                .build();
        virtualCardRepo.save(virtualCard);

        // ── Physical card (Oracle MTX_USER_PHYSICAL_CARD) ─────────────────
        MtxUserPhysicalCard physCard = MtxUserPhysicalCard.builder()
                .trackingNumber("PCARD001")
                .masterAccountId("MACC001")
                .partyId("100000001")
                .cardType("MC")
                .cardStatus("A")
                .cardNumber("5678")
                .accountNo("ACC001")
                .email("test@example.com")
                .phoneNumber("0981234567")
                .expiryDate("12/28")
                .maxDlyPurAmt(BigDecimal.valueOf(2000.00))
                .dlyPurLim(BigDecimal.valueOf(1000.00))
                .createdOn(now)
                .build();
        physicalCardRepo.save(physCard);

        log.info("[LocalOracleDataSeeder] Seeded Oracle source data. "
                + "Test customerKey: 100000001_ACC001_MACC001");
    }
}
