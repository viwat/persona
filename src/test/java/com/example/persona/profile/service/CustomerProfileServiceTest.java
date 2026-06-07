package com.example.persona.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.persona.dto.response.AccountDetail;
import com.example.persona.enums.StatusType;
import com.example.persona.profile.model.CustomerProfile;
import com.example.persona.profile.repository.CustomerProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerProfileService")
class CustomerProfileServiceTest {

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @InjectMocks
    private CustomerProfileService customerProfileService;

    @Nested
    @DisplayName("mergeCustomerProfileForMigration")
    class MergeCustomerProfileForMigration {

        @Test
        @DisplayName("creates new profile when none exists")
        void createsNewProfileWhenNoneExists() {
            when(customerProfileRepository.findByCustomerNo("C1")).thenReturn(Optional.empty());
            when(customerProfileRepository.save(any(CustomerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            var cdp = new AccountDetail.AccountDetailData();
            cdp.setCustomerType("I");
            cdp.setResidentStatus("R");
            cdp.setCategoryId("CAT1");
            cdp.setCategoryProfileName("Gold");
            cdp.setCategoryProfileId("CP1");

            customerProfileService.mergeCustomerProfileForMigration("C1", "Name One", "VERIFIED", cdp);

            ArgumentCaptor<CustomerProfile> captor = ArgumentCaptor.forClass(CustomerProfile.class);
            verify(customerProfileRepository).save(captor.capture());
            CustomerProfile saved = captor.getValue();
            assertThat(saved.getCustomerNo()).isEqualTo("C1");
            assertThat(saved.getCustomerName()).isEqualTo("Name One");
            assertThat(saved.getKycStatus()).isEqualTo("VERIFIED");
            assertThat(saved.getStatus()).isEqualTo(StatusType.ACTIVE);
            assertThat(saved.getCustomerType()).isEqualTo("I");
            assertThat(saved.getCustomerStatus()).isEqualTo("R");
            assertThat(saved.getCustomerCategory()).isEqualTo("CAT1");
            assertThat(saved.getCustomerClass()).isEqualTo("Gold");
            assertThat(saved.getCustomerSubCategory()).isEqualTo("CP1");
        }

        @Test
        @DisplayName("updates existing profile in place")
        void updatesExistingProfileInPlace() {
            CustomerProfile existing = new CustomerProfile();
            existing.setId(10L);
            existing.setCustomerNo("C1");
            existing.setCustomerName("Old");
            existing.setStatus(StatusType.ACTIVE);
            when(customerProfileRepository.findByCustomerNo("C1")).thenReturn(Optional.of(existing));

            customerProfileService.mergeCustomerProfileForMigration("C1", "New Name", "PENDING", null);

            ArgumentCaptor<CustomerProfile> captor = ArgumentCaptor.forClass(CustomerProfile.class);
            verify(customerProfileRepository).save(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(10L);
            assertThat(captor.getValue().getCustomerName()).isEqualTo("New Name");
            assertThat(captor.getValue().getKycStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("skips save when customerNo is blank")
        void skipsSaveWhenCustomerNoIsBlank() {
            customerProfileService.mergeCustomerProfileForMigration("", "N", "K", null);
            customerProfileService.mergeCustomerProfileForMigration(null, "N", "K", null);

            verify(customerProfileRepository, never()).save(any());
        }
    }
}
