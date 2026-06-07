package com.example.persona.migration.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.persona.client.CdpClient;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.migration.constants.MigrationConstants;
import com.example.persona.migration.dto.MigrationResult;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.model.oracle.DigiMasterAccountView;
import com.example.persona.migration.repository.oracle.DigiMasterAccountViewRepository;
import com.example.persona.profile.dto.request.CustomerAppProfileRequest;
import com.example.persona.profile.dto.response.CustomerAppProfileResponse;
import com.example.persona.profile.service.CustomerAppProfileService;
import com.example.persona.profile.service.CustomerProfileService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MasterAccountMigrationHandler Tests")
class MasterAccountMigrationHandlerTest {

    @Mock
    private DigiMasterAccountViewRepository digiMasterAccountViewRepository;

    @Mock
    private CustomerAppProfileService customerAppProfileService;

    @Mock
    private CustomerProfileService customerProfileService;

    @Mock
    private CdpClient cdpClient;

    @InjectMocks
    private MasterAccountMigrationHandler handler;

    private DigiMasterAccountView oracleData;
    private AccountDetail.AccountDetailData cdpData;
    private CustomerMigrationStage migrationStage;

    @BeforeEach
    void setUp() {
        oracleData = new DigiMasterAccountView();
        oracleData.setMasterAccId("MA123456");
        oracleData.setLoginId("999036090");
        oracleData.setUserName("John Doe");
        oracleData.setPhoneNumber("0123456789");
        oracleData.setPartyId("999036090");
        oracleData.setApplicationId(MigrationConstants.APPLICATION_ID_WINGPAY);
        oracleData.setStatus("ACTIVE");
        oracleData.setCreatedOn(LocalDateTime.now());

        cdpData = new AccountDetail.AccountDetailData();
        cdpData.setName("John Doe CDP");
        cdpData.setSex("M");
        cdpData.setNationality("KH");
        cdpData.setDataOfBirth("1990-05-15");
        cdpData.setMobileNumber("0987654321");

        migrationStage = new CustomerMigrationStage();
        migrationStage.setCustomerKey("999036090_099805807");
    }

    @Nested
    @DisplayName("getStageCode")
    class GetStageCode {

        @Test
        @DisplayName("should return MASTER_ACCOUNT stage code")
        void shouldReturnMasterAccountStageCode() {
            assertThat(handler.getStageCode()).isEqualTo("MASTER_ACCOUNT");
        }
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should complete migration successfully with Oracle and CDP data")
        void shouldCompleteMigrationSuccessfullyWithOracleAndCdpData() {
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo("099805807")).thenReturn(cdpData);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            MigrationResult result = handler.execute(migrationStage);

            assertThat(result.success()).isTrue();

            ArgumentCaptor<CustomerAppProfileRequest> captor = ArgumentCaptor.forClass(CustomerAppProfileRequest.class);
            verify(customerAppProfileService).mergeCustomerAppProfileForMigration(captor.capture());
            verify(customerProfileService)
                    .mergeCustomerProfileForMigration(eq("999036090"), eq("John Doe CDP"), eq("ACTIVE"), eq(cdpData));

            CustomerAppProfileRequest request = captor.getValue();
            assertThat(request.getCustomerNo()).isEqualTo("999036090");
            assertThat(request.getAccountNo()).isEqualTo("099805807");
            assertThat(request.getCustomerName()).isEqualTo("John Doe CDP");
            assertThat(request.getPhoneNo()).isEqualTo("0987654321");
            assertThat(request.getKycStatus()).isEqualTo("ACTIVE");
            assertThat(request.getGender()).isEqualTo("M");
            assertThat(request.getNationality()).isEqualTo("KH");
        }

        @Test
        @DisplayName("should complete migration with Oracle data only when CDP fails")
        void shouldCompleteMigrationWithOracleDataOnlyWhenCdpFails() {
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo(anyString())).thenThrow(new RuntimeException("CDP unavailable"));
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            MigrationResult result = handler.execute(migrationStage);

            assertThat(result.success()).isTrue();

            ArgumentCaptor<CustomerAppProfileRequest> captor = ArgumentCaptor.forClass(CustomerAppProfileRequest.class);
            verify(customerAppProfileService).mergeCustomerAppProfileForMigration(captor.capture());

            verify(customerProfileService)
                    .mergeCustomerProfileForMigration(eq("999036090"), isNull(), eq("ACTIVE"), isNull());

            CustomerAppProfileRequest request = captor.getValue();
            assertThat(request.getCustomerName()).isNull();
            assertThat(request.getKycStatus()).isEqualTo("ACTIVE");
            assertThat(request.getGender()).isNull();
            assertThat(request.getNationality()).isNull();
        }

        @Test
        @DisplayName("should persist CDP customer name when Oracle USER_NAME is login-like")
        void shouldPersistCdpCustomerNameWhenOracleUserNameIsLoginLike() {
            oracleData.setUserName("099805807");
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            cdpData.setName("Sok Dara");
            when(cdpClient.getAccountInfo("099805807")).thenReturn(cdpData);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            handler.execute(migrationStage);

            var captor = org.mockito.ArgumentCaptor.forClass(CustomerAppProfileRequest.class);
            verify(customerAppProfileService).mergeCustomerAppProfileForMigration(captor.capture());

            assertThat(captor.getValue().getCustomerName()).isEqualTo("Sok Dara");
            verify(customerProfileService)
                    .mergeCustomerProfileForMigration(eq("999036090"), eq("Sok Dara"), eq("ACTIVE"), eq(cdpData));
        }

        @Test
        @DisplayName("should use CDP name when Oracle USER_NAME is empty (still not used for display name)")
        void shouldUseCdpNameWhenOracleUserNameIsEmpty() {
            oracleData.setUserName(null);
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo("099805807")).thenReturn(cdpData);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            handler.execute(migrationStage);

            ArgumentCaptor<CustomerAppProfileRequest> captor = ArgumentCaptor.forClass(CustomerAppProfileRequest.class);
            verify(customerAppProfileService).mergeCustomerAppProfileForMigration(captor.capture());

            assertThat(captor.getValue().getCustomerName()).isEqualTo("John Doe CDP");
            verify(customerProfileService)
                    .mergeCustomerProfileForMigration(eq("999036090"), eq("John Doe CDP"), eq("ACTIVE"), eq(cdpData));
        }

        @Test
        @DisplayName("should use kyc_status from CDP when present")
        void shouldUseKycStatusFromCdpWhenPresent() {
            cdpData.setKycStatus("FULL_KYC");
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo("099805807")).thenReturn(cdpData);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            handler.execute(migrationStage);

            var captor = ArgumentCaptor.forClass(CustomerAppProfileRequest.class);
            verify(customerAppProfileService).mergeCustomerAppProfileForMigration(captor.capture());
            assertThat(captor.getValue().getKycStatus()).isEqualTo("FULL_KYC");
            verify(customerProfileService)
                    .mergeCustomerProfileForMigration(eq("999036090"), eq("John Doe CDP"), eq("FULL_KYC"), eq(cdpData));
        }

        @Test
        @DisplayName("should fail when Oracle data not found")
        void shouldFailWhenOracleDataNotFound() {
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            anyString(), eq(MigrationConstants.APPLICATION_ID_WINGPAY)))
                    .thenReturn(Optional.empty());

            MigrationResult result = handler.execute(migrationStage);

            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("ORACLE_DATA_NOT_FOUND");
            verify(customerAppProfileService, never()).mergeCustomerAppProfileForMigration(any());
            verify(customerProfileService, never()).mergeCustomerProfileForMigration(any(), any(), any(), any());
        }

        @Test
        @DisplayName("should handle null customer key gracefully")
        void shouldHandleNullCustomerKeyGracefully() {
            CustomerMigrationStage nullKeyStage = new CustomerMigrationStage();
            nullKeyStage.setCustomerKey(null);

            MigrationResult result = handler.execute(nullKeyStage);

            assertThat(result.success()).isFalse();
            verify(customerProfileService, never()).mergeCustomerProfileForMigration(any(), any(), any(), any());
        }

        @Test
        @DisplayName("should handle exception during profile creation")
        void shouldHandleExceptionDuringProfileCreation() {
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo(anyString())).thenReturn(null);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenThrow(new RuntimeException("Database error"));

            MigrationResult result = handler.execute(migrationStage);

            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("MASTER_ACCOUNT_ERROR");
            assertThat(result.errorMessage()).contains("Database error");
            verify(customerProfileService, never()).mergeCustomerProfileForMigration(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Data Source Lookup")
    class DataSourceLookup {

        @Test
        @DisplayName("should resolve via master account ID when WINGPAY login lookups miss")
        void shouldResolveViaMasterAccountIdWhenWingpayLoginLookupsMiss() {
            CustomerMigrationStage stageWithMasterId = new CustomerMigrationStage();
            // ACCOUNT_NO strategy: loginId == accountNo; both WINGPAY lookups miss before master id.
            stageWithMasterId.setCustomerKey("999036090_099805807_MA123456");

            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.empty());
            when(digiMasterAccountViewRepository.findByMasterAccId("MA123456")).thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo(anyString())).thenReturn(null);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            MigrationResult result = handler.execute(stageWithMasterId);

            assertThat(result.success()).isTrue();
            // loginId and accountNo are both 099805807 under ACCOUNT_NO strategy → two WINGPAY lookups before master.
            verify(digiMasterAccountViewRepository, atLeast(1))
                    .findByLoginIdAndApplicationId("099805807", MigrationConstants.APPLICATION_ID_WINGPAY);
            verify(digiMasterAccountViewRepository).findByMasterAccId("MA123456");
        }

        @Test
        @DisplayName("should resolve via WINGPAY login when key includes wrong master id (master lookup not needed)")
        void shouldResolveViaWingpayLoginWhenKeyIncludesWrongMasterId() {
            CustomerMigrationStage stageWithMasterId = new CustomerMigrationStage();
            stageWithMasterId.setCustomerKey("999036090_099805807_MA999999");

            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo(anyString())).thenReturn(null);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            MigrationResult result = handler.execute(stageWithMasterId);

            assertThat(result.success()).isTrue();
            verify(digiMasterAccountViewRepository, never()).findByMasterAccId(anyString());
        }
    }

    @Nested
    @DisplayName("Date Parsing")
    class DateParsing {

        @Test
        @DisplayName("should parse valid date of birth from CDP")
        void shouldParseValidDateOfBirthFromCdp() {
            cdpData.setDataOfBirth("1990-05-15");
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo("099805807")).thenReturn(cdpData);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            handler.execute(migrationStage);

            ArgumentCaptor<CustomerAppProfileRequest> captor = ArgumentCaptor.forClass(CustomerAppProfileRequest.class);
            verify(customerAppProfileService).mergeCustomerAppProfileForMigration(captor.capture());

            assertThat(captor.getValue().getDateOfBirth()).isNotNull();
            assertThat(captor.getValue().getDateOfBirth().getYear()).isEqualTo(1990);
            assertThat(captor.getValue().getDateOfBirth().getMonthValue()).isEqualTo(5);
            assertThat(captor.getValue().getDateOfBirth().getDayOfMonth()).isEqualTo(15);
        }

        @Test
        @DisplayName("should handle invalid date format gracefully")
        void shouldHandleInvalidDateFormatGracefully() {
            cdpData.setDataOfBirth("invalid-date");
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo("099805807")).thenReturn(cdpData);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            MigrationResult result = handler.execute(migrationStage);

            assertThat(result.success()).isTrue();

            ArgumentCaptor<CustomerAppProfileRequest> captor = ArgumentCaptor.forClass(CustomerAppProfileRequest.class);
            verify(customerAppProfileService).mergeCustomerAppProfileForMigration(captor.capture());

            assertThat(captor.getValue().getDateOfBirth()).isNull();
        }

        @Test
        @DisplayName("should handle null date of birth")
        void shouldHandleNullDateOfBirth() {
            cdpData.setDataOfBirth(null);
            when(digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                            "099805807", MigrationConstants.APPLICATION_ID_WINGPAY))
                    .thenReturn(Optional.of(oracleData));
            when(cdpClient.getAccountInfo("099805807")).thenReturn(cdpData);
            when(customerAppProfileService.mergeCustomerAppProfileForMigration(any()))
                    .thenReturn(CustomerAppProfileResponse.builder().build());

            handler.execute(migrationStage);

            ArgumentCaptor<CustomerAppProfileRequest> captor = ArgumentCaptor.forClass(CustomerAppProfileRequest.class);
            verify(customerAppProfileService).mergeCustomerAppProfileForMigration(captor.capture());

            assertThat(captor.getValue().getDateOfBirth()).isNull();
        }
    }
}
