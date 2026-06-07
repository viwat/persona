package com.example.persona.client;

import com.example.persona.dto.response.AccountDetail;
import com.example.persona.enums.ErrorCode;
import com.example.persona.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CdpClient {

    private final CdpHttpClient cdpHttpClient;

    public AccountDetail.AccountDetailData getAccountInfo(String accountNo) {
        if (!StringUtils.hasText(accountNo)) {
            log.warn("Account number cannot be empty.");
            throw new AppException("Account number cannot be empty.", ErrorCode.VALIDATION_ERROR);
        }

        log.info("Fetching account details for accountNo: {}", accountNo);

        try {
            AccountDetail response = cdpHttpClient.getAccountDetail(accountNo);
            if (response == null || !"SUC000".equals(response.getErrorCode())) {
                log.warn("No valid account details found for accountNo: {}", accountNo);
                return null;
            }
            AccountDetail.AccountDetailData data = response.getData();
            if (data != null) {
                log.info("Account detail fetched successfully: {}", data);
            }
            return data;
        } catch (ResourceAccessException e) {
            log.error("Request timed out or failed while fetching account details for accountNo: {}", accountNo, e);
            return null;
        } catch (RestClientException e) {
            log.error("HTTP error while fetching account details for accountNo: {}", accountNo, e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while fetching account details for accountNo: {}", accountNo, e);
            throw new AppException("Failed to fetch account details", ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
