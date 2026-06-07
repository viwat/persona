package com.example.persona.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.persona.client.CdpClient;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.profile.dto.request.CustomerAppProfileRequest;
import com.example.persona.profile.dto.response.CustomerAppProfileResponse;
import com.example.persona.profile.model.CustomerAppProfile;
import com.example.persona.profile.repository.CustomerAppProfileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
@DisplayName("CustomerAppProfileService Tests")
class CustomerAppProfileServiceTest {

    @Mock
    private CustomerAppProfileRepository customerAppProfileRepository;

    @Mock
    private ProfileVersioningService versioningService;

    @Mock
    private CdpClient cdpClient;

    @InjectMocks
    private CustomerAppProfileService customerAppProfileService;

    private CustomerAppProfile existingProfile;
    private CustomerAppProfileRequest validRequest;

    @BeforeEach
    void setUp() {
        existingProfile = new CustomerAppProfile();
        existingProfile.setId(1L);
        existingProfile.setCustomerNo("999036090");
        existingProfile.setCustomerAppId("999036090");
        existingProfile.setCustomerName("John Doe");
        existingProfile.setPhoneNo("0123456789");
        existingProfile.setKycStatus("VERIFIED");
        existingProfile.setGender("M");
        existingProfile.setNationality("KH");
        existingProfile.setDateOfBirth(LocalDate.of(1990, 1, 15));
        existingProfile.setPinLimitAmount(new BigDecimal("100.00"));
        existingProfile.setVersionKey("999036090");
        existingProfile.setStatus(StatusType.ACTIVE);

        validRequest = CustomerAppProfileRequest.builder()
                .customerNo("999036090")
                .customerName("John Doe")
                .phoneNo("0123456789")
                .channelCode("MOBAPP")
                .build();
    }

    @Nested
    @DisplayName("getCustomerAppProfile")
    class GetCustomerAppProfile {

        @Test
        @DisplayName("should return profile when customer exists")
        void shouldReturnProfileWhenCustomerExists() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));

            CustomerAppProfileResponse response = customerAppProfileService.getCustomerAppProfile("999036090");

            assertThat(response).isNotNull();
            assertThat(response.getCustomerNo()).isEqualTo("999036090");
            assertThat(response.getCustomerName()).isEqualTo("John Doe");
            assertThat(response.getGender()).isEqualTo("M");
            assertThat(response.getNationality()).isEqualTo("KH");
            verify(customerAppProfileRepository)
                    .findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc("999036090", StatusType.ACTIVE);
        }

        @Test
        @DisplayName("should enrich customer_name from CDP when stored value equals account number")
        void shouldEnrichCustomerNameFromCdpWhenStoredValueEqualsAccountNumber() {
            existingProfile.setCustomerName("099805807");
            existingProfile.setAccountNo("099805807");
            existingProfile.setCustomerNameKh(null);

            AccountDetail.AccountDetailData cdp = new AccountDetail.AccountDetailData();
            cdp.setName("Sok Dara");

            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));
            when(cdpClient.getAccountInfo("099805807")).thenReturn(cdp);

            CustomerAppProfileResponse response = customerAppProfileService.getCustomerAppProfile("999036090");

            assertThat(response.getCustomerName()).isEqualTo("Sok Dara");
            assertThat(response.getCustomerNameKh()).isEqualTo("Sok Dara");
        }

        @Test
        @DisplayName("should throw exception when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "UNKNOWN", StatusType.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerAppProfileService.getCustomerAppProfile("UNKNOWN"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Customer app profile not found");
        }

        @Test
        @DisplayName("should return profile by provided customer_key")
        void shouldReturnProfileByProvidedCustomerKey() {
            existingProfile.setCustomerKey("999036090_099805807");
            existingProfile.setVersionKey("999036090_099805807");
            when(customerAppProfileRepository.findByCustomerKeyAndStatus("999036090_099805807", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));

            CustomerBaseRequest req = CustomerBaseRequest.builder()
                    .customerKey("999036090_099805807")
                    .customerNo("999036090")
                    .build();

            CustomerAppProfileResponse response = customerAppProfileService.getCustomerAppProfile(req);
            assertThat(response.getCustomerNo()).isEqualTo("999036090");
            verify(customerAppProfileRepository).findByCustomerKeyAndStatus("999036090_099805807", StatusType.ACTIVE);
        }

        @Test
        @DisplayName("should return profile by customer_no and account_no composed key")
        void shouldReturnProfileByComposedCustomerKey() {
            existingProfile.setCustomerKey("999036090_099805807");
            existingProfile.setVersionKey("999036090_099805807");
            when(customerAppProfileRepository.findByCustomerKeyAndStatus("999036090_099805807", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));

            CustomerBaseRequest req = CustomerBaseRequest.builder()
                    .customerNo("999036090")
                    .accountNo("099805807")
                    .build();

            assertThat(customerAppProfileService.getCustomerAppProfile(req).getCustomerNo())
                    .isEqualTo("999036090");
        }

        @Test
        @DisplayName("should resolve customer_no alone using latest active row (DB orders duplicates)")
        void shouldResolveCustomerNoUsingLatestActiveRow() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));

            CustomerBaseRequest req =
                    CustomerBaseRequest.builder().customerNo("999036090").build();

            assertThat(customerAppProfileService.getCustomerAppProfile(req).getCustomerNo())
                    .isEqualTo("999036090");
            verify(customerAppProfileRepository)
                    .findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc("999036090", StatusType.ACTIVE);
        }

        @Test
        @DisplayName("should return profile when account_no alone matches (latest active)")
        void shouldReturnProfileByAccountNoWhenUnique() {
            existingProfile.setAccountNo("099805807");
            when(customerAppProfileRepository.findFirstByAccountNoAndStatusOrderByCreatedDateDescIdDesc(
                            "099805807", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));

            CustomerBaseRequest req =
                    CustomerBaseRequest.builder().accountNo("099805807").build();

            assertThat(customerAppProfileService.getCustomerAppProfile(req).getCustomerNo())
                    .isEqualTo("999036090");
        }
    }

    @Nested
    @DisplayName("createCustomerAppProfile")
    class CreateCustomerAppProfile {

        @Test
        @DisplayName("should create new profile successfully")
        void shouldCreateNewProfileSuccessfully() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.empty());
            when(customerAppProfileRepository.save(any(CustomerAppProfile.class)))
                    .thenAnswer(invocation -> {
                        CustomerAppProfile saved = invocation.getArgument(0);
                        saved.setId(1L);
                        return saved;
                    });

            CustomerAppProfileResponse response = customerAppProfileService.createCustomerAppProfile(validRequest);

            assertThat(response).isNotNull();
            verify(customerAppProfileRepository).save(any(CustomerAppProfile.class));
        }

        @Test
        @DisplayName("should throw exception when profile already exists")
        void shouldThrowExceptionWhenProfileAlreadyExists() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));

            assertThatThrownBy(() -> customerAppProfileService.createCustomerAppProfile(validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("updateCustomerAppProfile")
    class UpdateCustomerAppProfile {

        @Test
        @DisplayName("should update existing profile successfully")
        void shouldUpdateExistingProfileSuccessfully() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));
            when(customerAppProfileRepository.save(any(CustomerAppProfile.class)))
                    .thenAnswer(invocation -> {
                        CustomerAppProfile saved = invocation.getArgument(0);
                        saved.setId(2L);
                        return saved;
                    });

            CustomerAppProfileRequest updateRequest = CustomerAppProfileRequest.builder()
                    .customerNo("999036090")
                    .customerName("Jane Doe")
                    .gender("F")
                    .build();

            CustomerAppProfileResponse response = customerAppProfileService.updateCustomerAppProfile(updateRequest);

            assertThat(response).isNotNull();
            verify(versioningService).createNewVersion(eq(existingProfile), any(CustomerAppProfile.class), anyString());
        }

        @Test
        @DisplayName("should throw exception when profile not found for update")
        void shouldThrowExceptionWhenProfileNotFoundForUpdate() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "UNKNOWN", StatusType.ACTIVE))
                    .thenReturn(Optional.empty());

            CustomerAppProfileRequest updateRequest =
                    CustomerAppProfileRequest.builder().customerNo("UNKNOWN").build();

            assertThatThrownBy(() -> customerAppProfileService.updateCustomerAppProfile(updateRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("createOrUpdateCustomerAppProfile")
    class CreateOrUpdateCustomerAppProfile {

        @Test
        @DisplayName("should create when profile does not exist")
        void shouldCreateWhenProfileDoesNotExist() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.empty());
            when(customerAppProfileRepository.save(any(CustomerAppProfile.class)))
                    .thenAnswer(invocation -> {
                        CustomerAppProfile saved = invocation.getArgument(0);
                        saved.setId(1L);
                        return saved;
                    });

            CustomerAppProfileResponse response =
                    customerAppProfileService.createOrUpdateCustomerAppProfile(validRequest);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("should update when profile exists")
        void shouldUpdateWhenProfileExists() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));
            when(customerAppProfileRepository.save(any(CustomerAppProfile.class)))
                    .thenAnswer(invocation -> {
                        CustomerAppProfile saved = invocation.getArgument(0);
                        saved.setId(2L);
                        return saved;
                    });

            CustomerAppProfileResponse response =
                    customerAppProfileService.createOrUpdateCustomerAppProfile(validRequest);

            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("mergeCustomerAppProfileForMigration")
    class MergeCustomerAppProfileForMigration {

        @Test
        @DisplayName("should insert one row when no active profile exists")
        void shouldInsertOneRowWhenNoActiveProfileExists() {
            when(versioningService.generateCustomerVersionKey(any(), anyString()))
                    .thenReturn("999036090_099805807");
            when(customerAppProfileRepository.findByVersionKeyAndStatus("999036090_099805807", StatusType.ACTIVE))
                    .thenReturn(Optional.empty());
            when(customerAppProfileRepository.save(any(CustomerAppProfile.class)))
                    .thenAnswer(invocation -> {
                        CustomerAppProfile saved = invocation.getArgument(0);
                        saved.setId(10L);
                        return saved;
                    });

            CustomerAppProfileRequest req = CustomerAppProfileRequest.builder()
                    .customerNo("999036090")
                    .accountNo("099805807")
                    .customerKey("999036090_099805807")
                    .channelCode("MOBAPP")
                    .customerName("First")
                    .build();

            customerAppProfileService.mergeCustomerAppProfileForMigration(req);

            verify(customerAppProfileRepository, times(1)).save(any(CustomerAppProfile.class));
        }

        @Test
        @DisplayName("should update same row in place when active profile already exists")
        void shouldUpdateSameRowInPlaceWhenActiveProfileAlreadyExists() {
            when(versioningService.generateCustomerVersionKey(any(), anyString()))
                    .thenReturn("999036090_099805807");
            existingProfile.setId(99L);
            existingProfile.setVersionKey("999036090_099805807");
            existingProfile.setCustomerName("Before");
            when(customerAppProfileRepository.findByVersionKeyAndStatus("999036090_099805807", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));
            when(customerAppProfileRepository.save(any(CustomerAppProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            CustomerAppProfileRequest req = CustomerAppProfileRequest.builder()
                    .customerNo("999036090")
                    .accountNo("099805807")
                    .customerKey("999036090_099805807")
                    .channelCode("MOBAPP")
                    .customerName("After")
                    .build();

            customerAppProfileService.mergeCustomerAppProfileForMigration(req);

            verify(customerAppProfileRepository, times(1)).save(any(CustomerAppProfile.class));
            assertThat(existingProfile.getCustomerName()).isEqualTo("After");
        }
    }

    @Nested
    @DisplayName("updatePinLimit")
    class UpdatePinLimit {

        @Test
        @DisplayName("should update PIN limit successfully")
        void shouldUpdatePinLimitSuccessfully() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));
            when(customerAppProfileRepository.save(any(CustomerAppProfile.class)))
                    .thenAnswer(invocation -> {
                        CustomerAppProfile saved = invocation.getArgument(0);
                        saved.setId(2L);
                        return saved;
                    });

            BigDecimal newLimit = new BigDecimal("500.00");

            CustomerBaseRequest customerBaseRequest = new CustomerBaseRequest();
            customerBaseRequest.setCustomerNo("999036090");
            CustomerAppProfileResponse response =
                    customerAppProfileService.updatePinLimit(customerBaseRequest, newLimit);

            assertThat(response).isNotNull();

            ArgumentCaptor<CustomerAppProfile> captor = ArgumentCaptor.forClass(CustomerAppProfile.class);
            verify(customerAppProfileRepository, times(2)).save(captor.capture());

            List<CustomerAppProfile> savedVersions = captor.getAllValues();
            CustomerAppProfile newVersion = savedVersions.getLast();
            assertThat(newVersion.getPinLimitAmount()).isEqualTo(newLimit);
        }
    }

    @Nested
    @DisplayName("Field Mapping Tests")
    class FieldMappingTests {

        @Test
        @DisplayName("should map all personal information fields correctly")
        void shouldMapAllPersonalInformationFieldsCorrectly() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.empty());
            when(customerAppProfileRepository.save(any(CustomerAppProfile.class)))
                    .thenAnswer(invocation -> {
                        CustomerAppProfile saved = invocation.getArgument(0);
                        saved.setId(1L);
                        return saved;
                    });

            CustomerAppProfileRequest fullRequest = CustomerAppProfileRequest.builder()
                    .customerNo("999036090")
                    .customerName("Full Name")
                    .customerNameKh("ឈ្មោះពេញ")
                    .gender("M")
                    .maritalStatus("SINGLE")
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .placeOfBirth("Phnom Penh")
                    .nationality("KH")
                    .email("test@example.com")
                    .currentAddress("123 Main St")
                    .idType("PASSPORT")
                    .idNumber("A12345678")
                    .kycStatus("VERIFIED")
                    .pinLimitAmount(new BigDecimal("200.00"))
                    .channelCode("MOBAPP")
                    .build();

            customerAppProfileService.createCustomerAppProfile(fullRequest);

            ArgumentCaptor<CustomerAppProfile> captor = ArgumentCaptor.forClass(CustomerAppProfile.class);
            verify(customerAppProfileRepository).save(captor.capture());

            CustomerAppProfile saved = captor.getValue();
            assertThat(saved.getCustomerName()).isEqualTo("Full Name");
            assertThat(saved.getCustomerNameKh()).isEqualTo("ឈ្មោះពេញ");
            assertThat(saved.getGender()).isEqualTo("M");
            assertThat(saved.getMaritalStatus()).isEqualTo("SINGLE");
            assertThat(saved.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
            assertThat(saved.getPlaceOfBirth()).isEqualTo("Phnom Penh");
            assertThat(saved.getNationality()).isEqualTo("KH");
            assertThat(saved.getEmail()).isEqualTo("test@example.com");
            assertThat(saved.getCurrentAddress()).isEqualTo("123 Main St");
            assertThat(saved.getIdType()).isEqualTo("PASSPORT");
            assertThat(saved.getIdNumber()).isEqualTo("A12345678");
            assertThat(saved.getKycStatus()).isEqualTo("VERIFIED");
            assertThat(saved.getPinLimitAmount()).isEqualTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("should handle null optional fields gracefully")
        void shouldHandleNullOptionalFieldsGracefully() {
            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.empty());
            when(customerAppProfileRepository.save(any(CustomerAppProfile.class)))
                    .thenAnswer(invocation -> {
                        CustomerAppProfile saved = invocation.getArgument(0);
                        saved.setId(1L);
                        return saved;
                    });

            CustomerAppProfileRequest minimalRequest = CustomerAppProfileRequest.builder()
                    .customerNo("999036090")
                    .channelCode("MOBAPP")
                    .build();

            customerAppProfileService.createCustomerAppProfile(minimalRequest);

            ArgumentCaptor<CustomerAppProfile> captor = ArgumentCaptor.forClass(CustomerAppProfile.class);
            verify(customerAppProfileRepository).save(captor.capture());

            CustomerAppProfile saved = captor.getValue();
            assertThat(saved.getCustomerNo()).isEqualTo("999036090");
            assertThat(saved.getCustomerName()).isNull();
            assertThat(saved.getGender()).isNull();
            assertThat(saved.getEmail()).isNull();
        }
    }

    @Nested
    @DisplayName("Fallback Logic Tests")
    class FallbackLogicTests {

        @Test
        @DisplayName("should fallback customerNameKh to customerName when customerNameKh is null")
        void shouldFallbackCustomerNameKhToCustomerNameWhenNull() {
            existingProfile.setCustomerName("John Doe");
            existingProfile.setCustomerNameKh(null);

            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));

            CustomerAppProfileResponse response = customerAppProfileService.getCustomerAppProfile("999036090");

            assertThat(response.getCustomerNameKh()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("should fallback customerNameKh to customerName when customerNameKh is blank")
        void shouldFallbackCustomerNameKhToCustomerNameWhenBlank() {
            existingProfile.setCustomerName("John Doe");
            existingProfile.setCustomerNameKh("   ");

            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));

            CustomerAppProfileResponse response = customerAppProfileService.getCustomerAppProfile("999036090");

            assertThat(response.getCustomerNameKh()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("should use customerNameKh when it has value")
        void shouldUseCustomerNameKhWhenItHasValue() {
            existingProfile.setCustomerName("John Doe");
            existingProfile.setCustomerNameKh("ឈ្មោះខ្មែរ");

            when(customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                            "999036090", StatusType.ACTIVE))
                    .thenReturn(Optional.of(existingProfile));

            CustomerAppProfileResponse response = customerAppProfileService.getCustomerAppProfile("999036090");

            assertThat(response.getCustomerNameKh()).isEqualTo("ឈ្មោះខ្មែរ");
        }
    }
}
