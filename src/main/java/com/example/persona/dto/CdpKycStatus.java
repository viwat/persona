package com.example.persona.dto;

import com.example.persona.dto.response.AccountDetail;
import org.springframework.util.StringUtils;

/**
 * Resolves KYC status from CDP account detail. CDP may omit {@code kyc_status}; default is {@code ACTIVE}.
 */
public final class CdpKycStatus {

    public static final String DEFAULT_KYC_STATUS = "ACTIVE";

    private CdpKycStatus() {}

    public static String resolve(AccountDetail.AccountDetailData cdp) {
        if (cdp == null || !StringUtils.hasText(cdp.getKycStatus())) {
            return DEFAULT_KYC_STATUS;
        }
        return cdp.getKycStatus().trim();
    }
}
