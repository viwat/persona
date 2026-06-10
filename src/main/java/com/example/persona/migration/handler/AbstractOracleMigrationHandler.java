package com.example.persona.migration.handler;

import com.example.persona.client.CdpClient;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.migration.constants.MigrationConstants;
import com.example.persona.migration.model.oracle.DigiMasterAccountView;
import com.example.persona.migration.repository.oracle.DigiMasterAccountViewRepository;
import com.example.persona.migration.util.CustomerKeyParser.ParsedKey;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Base class for migration handlers that read source data from Oracle.
 *
 * <p>Provides a canonical Oracle lookup chain (loginId → accountNo → masterAccId),
 * soft CDP data fetching, and date-of-birth parsing. These three utilities were
 * previously copy-pasted across {@code MasterAccountMigrationHandler} and
 * {@code DeviceMigrationHandler}.
 *
 * <p>Concrete subclasses must invoke {@code super(repo, cdpClient)} via an explicit
 * constructor — see subclass implementations.
 */
@Slf4j
public abstract class AbstractOracleMigrationHandler implements MigrationStageHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected final DigiMasterAccountViewRepository digiMasterAccountViewRepository;
    protected final CdpClient cdpClient;

    protected AbstractOracleMigrationHandler(
            DigiMasterAccountViewRepository digiMasterAccountViewRepository, CdpClient cdpClient) {
        this.digiMasterAccountViewRepository = digiMasterAccountViewRepository;
        this.cdpClient = cdpClient;
    }

    /**
     * Looks up Oracle source data with a prioritised fallback chain:
     * <ol>
     *   <li>loginId + WINGPAY applicationId</li>
     *   <li>accountNo as loginId + WINGPAY applicationId</li>
     *   <li>masterAccId direct lookup</li>
     * </ol>
     */
    protected Optional<DigiMasterAccountView> getDataFromSource(ParsedKey key) {
        if (StringUtils.hasText(key.loginId())) {
            Optional<DigiMasterAccountView> result = digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                    key.loginId(), MigrationConstants.APPLICATION_ID_WINGPAY);
            if (result.isPresent()) {
                return result;
            }
        }

        if (StringUtils.hasText(key.accountNo())) {
            Optional<DigiMasterAccountView> result = digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                    key.accountNo(), MigrationConstants.APPLICATION_ID_WINGPAY);
            if (result.isPresent()) {
                return result;
            }
        }

        if (StringUtils.hasText(key.masterAccId())) {
            Optional<DigiMasterAccountView> result =
                    digiMasterAccountViewRepository.findByMasterAccId(key.masterAccId());
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    /**
     * Fetches CDP account data. Failures are soft — returns {@code null} so the
     * caller continues migration with Oracle data only.
     */
    protected AccountDetail.AccountDetailData fetchCdpData(String accountNo) {
        if (!StringUtils.hasText(accountNo)) {
            return null;
        }
        try {
            AccountDetail.AccountDetailData cdpData = cdpClient.getAccountInfo(accountNo);
            if (cdpData != null) {
                log.debug("Found CDP data for accountNo: {}", accountNo);
            }
            return cdpData;
        } catch (Exception e) {
            log.warn("Failed to fetch CDP data for accountNo: {} — {}", accountNo, e.getMessage());
            return null;
        }
    }

    /**
     * Parses a {@code yyyy-MM-dd} date string into a {@link LocalDate}.
     * Returns {@code null} on blank input or parse failure.
     */
    protected LocalDate parseDateOfBirth(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.debug("Could not parse date of birth: {} — {}", dateStr, e.getMessage());
            return null;
        }
    }
}
