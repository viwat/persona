package com.example.persona.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.persona.dto.response.AccountDetail;
import org.junit.jupiter.api.Test;

class CdpKycStatusTest {

    @Test
    void resolve_defaultsToActiveWhenCdpNullOrKycBlank() {
        assertThat(CdpKycStatus.resolve(null)).isEqualTo("ACTIVE");
        AccountDetail.AccountDetailData data = new AccountDetail.AccountDetailData();
        data.setKycStatus(null);
        assertThat(CdpKycStatus.resolve(data)).isEqualTo("ACTIVE");
        data.setKycStatus("   ");
        assertThat(CdpKycStatus.resolve(data)).isEqualTo("ACTIVE");
    }

    @Test
    void resolve_trimsCdpValue() {
        AccountDetail.AccountDetailData data = new AccountDetail.AccountDetailData();
        data.setKycStatus("  PENDING  ");
        assertThat(CdpKycStatus.resolve(data)).isEqualTo("PENDING");
    }
}
