package com.example.persona.profile.service;

import com.example.persona.dto.CdpKycStatus;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.enums.StatusType;
import com.example.persona.profile.model.CustomerProfile;
import com.example.persona.profile.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Core customer profile (master) persistence. Migration uses {@link #mergeCustomerProfileForMigration}
 * to upsert a single row by {@code customer_no} without versioning.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;

    /**
     * Inserts or updates {@code dgtl_customer_profile} in place for migration. Non-blank incoming
     * fields overwrite existing values; CDP-derived fields are applied only when present on {@code cdp}.
     */
    @Transactional
    public void mergeCustomerProfileForMigration(
            String customerNo, String customerName, String kycStatus, AccountDetail.AccountDetailData cdp) {
        if (!StringUtils.hasText(customerNo)) {
            log.warn("Skip customer profile migration merge: customerNo is blank");
            return;
        }

        CustomerProfile entity = findOrCreateProfile(customerNo);
        if (entity.getStatus() == null) {
            entity.setStatus(StatusType.ACTIVE);
        }
        if (StringUtils.hasText(customerName)) {
            entity.setCustomerName(customerName);
        }
        entity.setKycStatus(StringUtils.hasText(kycStatus) ? kycStatus.trim() : CdpKycStatus.resolve(cdp));
        applyCdpMigrationFields(entity, cdp);

        customerProfileRepository.save(entity);
        log.info("Migration customer profile merged for customerNo: {}", customerNo);
    }

    private CustomerProfile findOrCreateProfile(String customerNo) {
        return customerProfileRepository.findByCustomerNo(customerNo).orElseGet(() -> {
            CustomerProfile p = new CustomerProfile();
            p.setCustomerNo(customerNo);
            p.setStatus(StatusType.ACTIVE);
            return p;
        });
    }

    private static void applyCdpMigrationFields(CustomerProfile entity, AccountDetail.AccountDetailData cdp) {
        if (cdp == null) {
            return;
        }
        if (StringUtils.hasText(cdp.getCustomerType())) {
            entity.setCustomerType(cdp.getCustomerType());
        }
        if (StringUtils.hasText(cdp.getResidentStatus())) {
            entity.setCustomerStatus(cdp.getResidentStatus());
        }
        applyCustomerCategoryFromCdp(entity, cdp);
        if (StringUtils.hasText(cdp.getDomainId())) {
            entity.setCustomerSegment(cdp.getDomainId());
        }
        if (StringUtils.hasText(cdp.getCategoryName())) {
            entity.setCustomerGroup(cdp.getCategoryName());
        }
        if (StringUtils.hasText(cdp.getCategoryProfileName())) {
            entity.setCustomerClass(cdp.getCategoryProfileName());
        }
        if (StringUtils.hasText(cdp.getCategoryProfileId())) {
            entity.setCustomerSubCategory(cdp.getCategoryProfileId());
        }
    }

    private static void applyCustomerCategoryFromCdp(CustomerProfile entity, AccountDetail.AccountDetailData cdp) {
        if (StringUtils.hasText(cdp.getCustomerCategory())) {
            entity.setCustomerCategory(cdp.getCustomerCategory());
        } else if (StringUtils.hasText(cdp.getCategoryId())) {
            entity.setCustomerCategory(cdp.getCategoryId());
        }
    }
}
