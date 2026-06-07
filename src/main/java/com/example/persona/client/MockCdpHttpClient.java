package com.example.persona.client;

import com.example.persona.dto.response.AccountDetail;
import java.math.BigDecimal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class MockCdpHttpClient implements CdpHttpClient {

    @Override
    public AccountDetail getAccountDetail(String accountNo) {
        AccountDetail.AccountDetailData data = new AccountDetail.AccountDetailData();
        data.setAccountNo(accountNo);
        data.setCustomerNo("MOCK-CUST-001");
        data.setName("Mock User");
        data.setFirstName("Mock");
        data.setLastName("User");
        data.setFirstNameInKhmer("ម៉ូក");
        data.setLastNameInKhmer("យូស័រ");
        data.setMsisdn("0981234567");
        data.setEmail("mock.user@example.com");
        data.setCcy("USD");
        data.setAccountType("SAVINGS");
        data.setAccountClass("RETAIL");
        data.setCategoryId("CAT001");
        data.setCategoryProfileName("Standard");
        data.setKycStatus("VERIFIED");
        data.setSex("M");
        data.setNationality("KH");
        data.setDataOfBirth("1990-01-15");
        data.setPlaceOfBirth("Phnom Penh");
        data.setMaritalStatus("SINGLE");
        data.setUniqueIdName("NATIONAL_ID");
        data.setUniqueIdValue("012345678");
        data.setUniqueIdExp("2030-12-31");
        data.setAddress("123 Mock Street, Phnom Penh");
        data.setCurrentBalance(BigDecimal.valueOf(5000.00));
        data.setAvailableBalance(BigDecimal.valueOf(4800.00));
        data.setResidentStatus("ACTIVE");

        AccountDetail response = new AccountDetail();
        response.setErrorCode("SUC000");
        response.setMessage("Success");
        response.setData(data);
        return response;
    }
}
