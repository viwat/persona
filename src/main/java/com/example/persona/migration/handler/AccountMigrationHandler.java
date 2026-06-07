package com.example.persona.migration.handler;

import com.example.persona.client.CdpClient;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.migration.dto.MigrationResult;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.util.CustomerKeyParser;
import com.example.persona.profile.dto.request.AccountAppManagementRequest;
import com.example.persona.profile.dto.response.AccountAppProfileResponse;
import com.example.persona.profile.model.AccountProfile;
import com.example.persona.profile.repository.AccountProfileRepository;
import com.example.persona.profile.service.AccountAppProfileService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountMigrationHandler implements MigrationStageHandler {

    private final CdpClient cdpClient;
    private final AccountAppProfileService accountAppProfileService;
    private final AccountProfileRepository accountProfileRepository;

    @Override
    public String getStageCode() {
        return "ACCOUNT";
    }

    @Override
    public MigrationResult execute(CustomerMigrationStage customerMigrationStage) {
        String customerKey = customerMigrationStage != null ? customerMigrationStage.getCustomerKey() : null;
        log.info("Starting Account migration for customer: {}", customerKey);
        try {
            CustomerKeyParser.ParsedKey parsedKey = CustomerKeyParser.parse(
                    StringUtils.hasText(customerKey) ? customerKey : "", CustomerKeyParser.LoginIdStrategy.ACCOUNT_NO);
            if (parsedKey.accountNo() == null) {
                log.warn("Account migration skipped: missing accountNo in key: {}", customerKey);
                return MigrationResult.failure("ACCOUNT_ERROR", "Missing accountNo in customer key");
            }

            AccountDetail.AccountDetailData accountDetailData = cdpClient.getAccountInfo(parsedKey.accountNo());
            if (accountDetailData == null) {
                throw new IllegalArgumentException("Account data not found for key: " + customerKey);
            }

            BigDecimal availableBalance = accountDetailData.getAvailableBalance();

            AccountProfile accountProfile = AccountProfile.builder()
                    .accountNo(accountDetailData.getAccountNo())
                    .customerNo(accountDetailData.getCustomerNo())
                    .accountCategory(accountDetailData.getCategoryId())
                    .accountName(accountDetailData.getName())
                    .accountType(accountDetailData.getAccountType())
                    .accountStatus(accountDetailData.getResidentStatus())
                    .classOfService(accountDetailData.getCategoryProfileName())
                    .balance(availableBalance != null ? availableBalance : BigDecimal.ZERO)
                    .status(com.example.persona.enums.StatusType.ACTIVE)
                    .build();
            accountProfileRepository.save(accountProfile);

            AccountAppManagementRequest accountAppManagementRequest = AccountAppManagementRequest.builder()
                    .accountNo(accountProfile.getAccountNo())
                    .customerNo(accountProfile.getCustomerNo())
                    .masterAccountNo("")
                    .accountName(accountProfile.getAccountName())
                    .accountHolderName(accountDetailData.getName())
                    .phoneNo(accountDetailData.getMsisdn())
                    .accountClass(accountDetailData.getAccountClass())
                    .accountType(accountDetailData.getAccountType())
                    .accountCategory(accountDetailData.getCategoryId())
                    .accountStatus(accountDetailData.getResidentStatus())
                    .classOfService(accountDetailData.getCategoryProfileName())
                    .currency(accountDetailData.getCcy())
                    .balance(availableBalance != null ? availableBalance : BigDecimal.ZERO)
                    .build();

            AccountAppProfileResponse accountAppProfileResponse =
                    accountAppProfileService.createOrUpdateAccountAppProfile(accountAppManagementRequest);
            log.info("Account profile response {} ", accountAppProfileResponse);

            log.info("Account migration completed for customer: {}", customerKey);
            return MigrationResult.completed();

        } catch (Exception e) {
            log.error("Account migration failed for customer: {}", customerKey, e);
            return MigrationResult.failure("ACCOUNT_ERROR", e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }
}
