package com.example.persona.migration.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.persona.dto.response.AccountDetail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomerProfileMigrationFieldsTest {

    @Test
    @DisplayName("customer name uses CDP full_name when present")
    void customerNameUsesCdpFullName() {
        var cdp = new AccountDetail.AccountDetailData();
        cdp.setName("Sok Dara");

        assertThat(CustomerProfileMigrationFields.resolveCustomerNameForMigration(cdp))
                .isEqualTo("Sok Dara");
    }

    @Test
    @DisplayName("customer name ignores Oracle USER_NAME semantics — only CDP")
    void customerNameIsCdpOnly() {
        var cdp = new AccountDetail.AccountDetailData();
        cdp.setName("CDP Legal Name");

        assertThat(CustomerProfileMigrationFields.resolveCustomerNameForMigration(cdp))
                .isEqualTo("CDP Legal Name");
    }

    @Test
    @DisplayName("customer name is null when CDP absent")
    void customerNameNullWhenCdpAbsent() {
        assertThat(CustomerProfileMigrationFields.resolveCustomerNameForMigration(null))
                .isNull();
    }

    @Test
    @DisplayName("customer name is null when CDP has no name fields")
    void customerNameNullWhenCdpEmpty() {
        assertThat(CustomerProfileMigrationFields.resolveCustomerNameForMigration(
                        new AccountDetail.AccountDetailData()))
                .isNull();
    }

    @Test
    @DisplayName("phone prefers CDP mobile over masked Oracle value")
    void phonePrefersCdpMobileOverOracle() {
        var cdp = new AccountDetail.AccountDetailData();
        cdp.setMobileNumber("012345678");

        String phone = CustomerProfileMigrationFields.resolvePhoneForMigration("XXXXXXX", cdp);

        assertThat(phone).isEqualTo("012345678");
    }

    @Test
    @DisplayName("phone falls back to Oracle when CDP has no numbers")
    void phoneFallsBackToOracle() {
        String phone = CustomerProfileMigrationFields.resolvePhoneForMigration("011222333", null);

        assertThat(phone).isEqualTo("011222333");
    }

    @Test
    @DisplayName("address uses address lines when address field is blank")
    void addressUsesLinesWhenAddressBlank() {
        var cdp = new AccountDetail.AccountDetailData();
        cdp.setAddressLine1("St 1");
        cdp.setAddressLine2("Phnom Penh");

        String addr = CustomerProfileMigrationFields.resolveCurrentAddress(cdp);

        assertThat(addr).isEqualTo("St 1, Phnom Penh");
    }

    @Test
    @DisplayName("parseUniqueIdExpiration parses ISO date")
    void parseUniqueIdExpirationIsoDate() {
        LocalDateTime exp = CustomerProfileMigrationFields.parseUniqueIdExpiration("2030-01-15");

        assertThat(exp).isEqualTo(LocalDate.of(2030, 1, 15).atStartOfDay());
    }

    @Test
    @DisplayName("uses composed CDP first/middle/last when full_name is blank")
    void usesComposedCdpNameWhenFullNameMissing() {
        var cdp = new AccountDetail.AccountDetailData();
        cdp.setFirstName("Jane");
        cdp.setMiddleName("Q");
        cdp.setLastName("Doe");

        assertThat(CustomerProfileMigrationFields.resolveCustomerNameForMigration(cdp))
                .isEqualTo("Jane Q Doe");
    }

    @Test
    @DisplayName("uses customer_name1 when full_name and parts absent")
    void usesCustomerName1Fallback() {
        var cdp = new AccountDetail.AccountDetailData();
        cdp.setCustomerName1("Legacy Name One");

        assertThat(CustomerProfileMigrationFields.resolveCustomerNameForMigration(cdp))
                .isEqualTo("Legacy Name One");
    }
}
