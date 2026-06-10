package com.example.persona.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.exception.BusinessException;
import com.example.persona.exception.GlobalExceptionHandler;
import com.example.persona.profile.dto.request.CustomerAppProfileRequest;
import com.example.persona.profile.dto.request.UpdatePinLimitRequest;
import com.example.persona.profile.dto.response.AccountAppProfileResponse;
import com.example.persona.profile.dto.response.CustomerAppProfileResponse;
import com.example.persona.profile.service.AccountAppProfileService;
import com.example.persona.profile.service.CardAppProfileService;
import com.example.persona.profile.service.CustomerAppProfileService;
import com.example.persona.profile.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.tracing.Tracer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileController Tests")
class ProfileControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ProfileService profileService;

    @Mock
    private CustomerAppProfileService customerAppProfileService;

    @Mock
    private AccountAppProfileService accountAppProfileService;

    @Mock
    private CardAppProfileService cardAppProfileService;

    @InjectMocks
    private ProfileController profileController;

    private CustomerAppProfileResponse customerAppProfileResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new GlobalExceptionHandler(mock(Tracer.class)))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        customerAppProfileResponse = CustomerAppProfileResponse.builder()
                .customerNo("999036090")
                .customerName("John Doe")
                .customerNameKh("ឈ្មោះ")
                .gender("M")
                .nationality("KH")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phoneNo("0123456789")
                .kycStatus("VERIFIED")
                .pinLimitAmount(new BigDecimal("100.00"))
                .build();
    }

    @Nested
    @DisplayName("POST /profile/customer-app")
    class GetCustomerAppProfile {

        @Test
        @DisplayName("should return customer app profile successfully")
        void shouldReturnCustomerAppProfileSuccessfully() throws Exception {
            when(customerAppProfileService.getCustomerAppProfile(any(CustomerBaseRequest.class)))
                    .thenReturn(customerAppProfileResponse);

            CustomerBaseRequest request = new CustomerBaseRequest();
            request.setCustomerNo("999036090");

            mockMvc.perform(post("/profile/customer-app")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.customer_no").value("999036090"))
                    .andExpect(jsonPath("$.data.customer_name").value("John Doe"))
                    .andExpect(jsonPath("$.data.gender").value("M"))
                    .andExpect(jsonPath("$.data.nationality").value("KH"));
        }

        @Test
        @DisplayName("should return error when customer not found")
        void shouldReturnErrorWhenCustomerNotFound() throws Exception {
            when(customerAppProfileService.getCustomerAppProfile(any(CustomerBaseRequest.class)))
                    .thenThrow(new BusinessException("Customer app profile not found"));

            CustomerBaseRequest request = new CustomerBaseRequest();
            request.setCustomerNo("UNKNOWN");

            mockMvc.perform(post("/profile/customer-app")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /profile/customer-app/create")
    class CreateCustomerAppProfile {

        @Test
        @DisplayName("should create customer app profile successfully")
        void shouldCreateCustomerAppProfileSuccessfully() throws Exception {
            when(customerAppProfileService.createCustomerAppProfile(any(CustomerAppProfileRequest.class)))
                    .thenReturn(customerAppProfileResponse);

            CustomerAppProfileRequest request = CustomerAppProfileRequest.builder()
                    .customerNo("999036090")
                    .customerName("John Doe")
                    .phoneNo("0123456789")
                    .channelCode("MOBAPP")
                    .build();

            mockMvc.perform(post("/profile/customer-app/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.customer_no").value("999036090"));
        }

        @Test
        @DisplayName("should create profile with all personal information fields")
        void shouldCreateProfileWithAllPersonalInformationFields() throws Exception {
            CustomerAppProfileResponse fullResponse = CustomerAppProfileResponse.builder()
                    .customerNo("999036090")
                    .customerName("Full Name")
                    .customerNameKh("ឈ្មោះពេញ")
                    .gender("F")
                    .maritalStatus("MARRIED")
                    .dateOfBirth(LocalDate.of(1985, 12, 25))
                    .placeOfBirth("Siem Reap")
                    .nationality("KH")
                    .email("test@example.com")
                    .currentAddress("456 Street")
                    .idType("NATIONAL_ID")
                    .idNumber("123456789012")
                    .kycStatus("VERIFIED")
                    .build();

            when(customerAppProfileService.createCustomerAppProfile(any())).thenReturn(fullResponse);

            CustomerAppProfileRequest request = CustomerAppProfileRequest.builder()
                    .customerNo("999036090")
                    .customerName("Full Name")
                    .customerNameKh("ឈ្មោះពេញ")
                    .gender("F")
                    .maritalStatus("MARRIED")
                    .dateOfBirth(LocalDate.of(1985, 12, 25))
                    .placeOfBirth("Siem Reap")
                    .nationality("KH")
                    .email("test@example.com")
                    .currentAddress("456 Street")
                    .idType("NATIONAL_ID")
                    .idNumber("123456789012")
                    .kycStatus("VERIFIED")
                    .channelCode("MOBAPP")
                    .build();

            mockMvc.perform(post("/profile/customer-app/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.customer_name_kh").value("ឈ្មោះពេញ"))
                    .andExpect(jsonPath("$.data.gender").value("F"))
                    .andExpect(jsonPath("$.data.marital_status").value("MARRIED"))
                    .andExpect(jsonPath("$.data.place_of_birth").value("Siem Reap"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }
    }

    @Nested
    @DisplayName("PUT /profile/customer-app")
    class UpdateCustomerAppProfile {

        @Test
        @DisplayName("should update customer app profile successfully")
        void shouldUpdateCustomerAppProfileSuccessfully() throws Exception {
            CustomerAppProfileResponse updatedResponse = CustomerAppProfileResponse.builder()
                    .customerNo("999036090")
                    .customerName("Jane Doe Updated")
                    .gender("F")
                    .build();

            when(customerAppProfileService.updateCustomerAppProfile(any(CustomerAppProfileRequest.class)))
                    .thenReturn(updatedResponse);

            CustomerAppProfileRequest request = CustomerAppProfileRequest.builder()
                    .customerNo("999036090")
                    .customerName("Jane Doe Updated")
                    .gender("F")
                    .build();

            mockMvc.perform(put("/profile/customer-app")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.customer_name").value("Jane Doe Updated"))
                    .andExpect(jsonPath("$.data.gender").value("F"));
        }
    }

    @Nested
    @DisplayName("PUT /profile/customer-app/pin-limit")
    class UpdatePinLimit {

        @Test
        @DisplayName("should update PIN limit successfully")
        void shouldUpdatePinLimitSuccessfully() throws Exception {
            CustomerAppProfileResponse updatedResponse = CustomerAppProfileResponse.builder()
                    .customerNo("999036090")
                    .pinLimitAmount(new BigDecimal("500.00"))
                    .build();

            when(customerAppProfileService.updatePinLimit(any(UpdatePinLimitRequest.class), any(BigDecimal.class)))
                    .thenReturn(updatedResponse);

            UpdatePinLimitRequest request = new UpdatePinLimitRequest();
            request.setCustomerNo("999036090");
            request.setPinLimitAmount(new BigDecimal("500.00"));

            mockMvc.perform(put("/profile/customer-app/pin-limit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pin_limit_amount").value(500.00));
        }
    }

    @Nested
    @DisplayName("POST /profile/account")
    class GetAccountProfiles {

        @Test
        @DisplayName("should return visible account profiles successfully")
        void shouldReturnVisibleAccountProfilesSuccessfully() throws Exception {
            List<AccountAppProfileResponse> accounts = Arrays.asList(
                    AccountAppProfileResponse.builder()
                            .accountNo("001234567890")
                            .accountName("Savings Account")
                            .accountHolderName("John Doe")
                            .currency("USD")
                            .balance(new BigDecimal("1000.00"))
                            .isHidden(false)
                            .build(),
                    AccountAppProfileResponse.builder()
                            .accountNo("001234567891")
                            .accountName("Current Account")
                            .accountHolderName("John Doe")
                            .currency("KHR")
                            .balance(new BigDecimal("5000000.00"))
                            .isHidden(false)
                            .build());

            when(accountAppProfileService.getVisibleAccountProfiles("999036090"))
                    .thenReturn(accounts);

            CustomerBaseRequest request = new CustomerBaseRequest();
            request.setCustomerNo("999036090");

            mockMvc.perform(post("/profile/account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].account_no").value("001234567890"))
                    .andExpect(jsonPath("$.data[0].currency").value("USD"))
                    .andExpect(jsonPath("$.data[1].currency").value("KHR"));
        }
    }

    @Nested
    @DisplayName("Nullable Fields in Response")
    class NullableFieldsInResponse {

        @Test
        @DisplayName("should return profile with null optional fields")
        void shouldReturnProfileWithNullOptionalFields() throws Exception {
            CustomerAppProfileResponse minimalResponse = CustomerAppProfileResponse.builder()
                    .customerNo("999036090")
                    .customerName("John Doe")
                    .phoneNo("0123456789")
                    .build();

            when(customerAppProfileService.getCustomerAppProfile(any(CustomerBaseRequest.class)))
                    .thenReturn(minimalResponse);

            CustomerBaseRequest request = new CustomerBaseRequest();
            request.setCustomerNo("999036090");

            mockMvc.perform(post("/profile/customer-app")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.customer_no").value("999036090"))
                    .andExpect(jsonPath("$.data.customer_name").value("John Doe"))
                    .andExpect(jsonPath("$.data.gender").doesNotExist())
                    .andExpect(jsonPath("$.data.nationality").doesNotExist())
                    .andExpect(jsonPath("$.data.email").doesNotExist())
                    .andExpect(jsonPath("$.data.date_of_birth").doesNotExist());
        }
    }
}
