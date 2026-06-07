package com.example.persona.transaction.service;

import com.example.persona.client.CdpClient;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.enums.AccountType;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.transaction.constants.ServiceTypeConstants;
import com.example.persona.transaction.dto.TxnLimitResponse;
import com.example.persona.transaction.dto.request.TxnLimitRequest;
import com.example.persona.transaction.model.LimitThreshold;
import com.example.persona.transaction.model.TransactionLimit;
import com.example.persona.transaction.model.TransactionLimitHistory;
import com.example.persona.transaction.repository.LimitThresholdRepository;
import com.example.persona.transaction.repository.TransactionLimitHistoryRepository;
import com.example.persona.transaction.repository.TransactionLimitRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionLimitService {

    private static final BigDecimal MONTHLY_LIMIT_MULTIPLIER = new BigDecimal("30");
    private static final String DEFAULT_CHANNEL_CODE = "MOBAPP";
    private static final String DAILY_AMOUNT_TYPE = "DAILY";

    private final CdpClient cdpClient;
    private final TransactionLimitRepository transactionLimitRepository;
    private final TransactionLimitHistoryRepository transactionLimitHistoryRepository;
    private final LimitThresholdRepository limitThresholdRepository;

    @Transactional
    public TxnLimitResponse createTransactionLimit(TxnLimitRequest request) {
        validateCreateRequest(request);

        checkLimitNotExists(request.getAccountNo(), request.getServiceType());
        enrichRequestWithAccountDetails(request);

        TransactionLimit limit = buildTransactionLimit(request);
        limit.setStatus(StatusType.ACTIVE);
        TransactionLimit savedLimit = transactionLimitRepository.save(limit);

        createHistoryRecord(savedLimit);
        return TxnLimitResponse.from(savedLimit);
    }

    @Transactional
    public TxnLimitResponse updateTransactionLimit(TxnLimitRequest request) {
        TransactionLimit limit = findLimitForUpdate(request);
        if (limit == null) {
            throw new BusinessException(
                    "Transaction limit not found. Please provide customerKey+serviceType, accountNo+serviceType, or limitSn");
        }

        createHistoryRecord(limit);
        validateAndCheckDuplicates(limit, request);
        updateLimitFields(limit, request);

        limit.setStatus(StatusType.ACTIVE);
        TransactionLimit updatedLimit = transactionLimitRepository.save(limit);
        return TxnLimitResponse.from(updatedLimit);
    }

    @Transactional
    public void deleteTransactionLimit(TxnLimitRequest request) {
        if (request.getLimitSn() == null) {
            throw new BusinessException("limitSn is required for delete operation");
        }

        TransactionLimit limit = transactionLimitRepository
                .findById(request.getLimitSn())
                .orElseThrow(() ->
                        new BusinessException("Transaction limit not found with limitSn: " + request.getLimitSn()));

        createHistoryRecord(limit);
        limit.setStatus(StatusType.INACTIVE);
        transactionLimitRepository.save(limit);
    }

    @Transactional(readOnly = true)
    public TxnLimitResponse getTransactionLimit(TxnLimitRequest request) {
        if (!StringUtils.hasText(request.getAccountNo()) || !StringUtils.hasText(request.getServiceType())) {
            throw new BusinessException("accountNo and serviceType are required to get transaction limit");
        }

        TransactionLimit limit = transactionLimitRepository
                .findActiveByAccountNoAndServiceType(request.getAccountNo(), request.getServiceType())
                .orElseThrow(() -> new BusinessException(String.format(
                        "Transaction limit not found for accountNo: %s and serviceType: %s",
                        request.getAccountNo(), request.getServiceType())));

        return TxnLimitResponse.from(limit);
    }

    @Transactional(readOnly = true)
    public List<TxnLimitResponse> getTransactionLimits(TxnLimitRequest request) {
        List<TransactionLimit> limits = findLimitsByRequest(request);
        return limits.stream().map(TxnLimitResponse::from).toList();
    }

    private void validateCreateRequest(TxnLimitRequest request) {
        if (!StringUtils.hasText(request.getAccountNo())) {
            throw new BusinessException("accountNo is required");
        }
        if (!StringUtils.hasText(request.getServiceType())) {
            throw new BusinessException("serviceType is required");
        }
        validateServiceType(request.getServiceType());
        if (request.getDailyLimit() == null || request.getDailyLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("dailyLimit is required and must be greater than zero");
        }
        validateLimitAgainstThresholds(request.getDailyLimit(), request.getServiceType());
    }

    private void validateServiceType(String serviceType) {
        if (!ServiceTypeConstants.isValidServiceType(serviceType)) {
            throw new BusinessException(
                    String.format("Invalid serviceType: %s. Please provide a valid service type.", serviceType));
        }
    }

    private TransactionLimit buildTransactionLimit(TxnLimitRequest request) {
        String customerNo =
                Optional.ofNullable(request.getCustomerNo()).orElse(extractCustomerNo(request.getCustomerKey()));
        String customerKey = buildCustomerKey(customerNo, request.getAccountNo(), request.getCustomerKey());
        BigDecimal dailyLimit = request.getDailyLimit();
        BigDecimal monthlyLimit = dailyLimit.multiply(MONTHLY_LIMIT_MULTIPLIER);

        return TransactionLimit.builder()
                .customerKey(customerKey)
                .accountNo(request.getAccountNo())
                .customerNo(customerNo)
                .masterAccountId(request.getAccountNo())
                .masterAccountNo(
                        Optional.ofNullable(request.getMasterAccountNo()).orElse(request.getAccountNo()))
                .serviceType(request.getServiceType())
                .channelCode(DEFAULT_CHANNEL_CODE)
                .accountType(AccountType.SAVINGS)
                .dailyLimit(dailyLimit)
                .monthlyLimit(monthlyLimit)
                .perTransactionLimit(dailyLimit)
                .dailyTransactionCount(null)
                .monthlyTransactionCount(null)
                .data(null)
                .metadata(null)
                .build();
    }

    private String extractCustomerNo(String customerKey) {
        if (!StringUtils.hasText(customerKey)) {
            return null;
        }
        String[] parts = customerKey.split("_");
        return parts.length > 0 ? parts[0] : customerKey;
    }

    private void createHistoryRecord(TransactionLimit limit) {
        try {
            TransactionLimitHistory history = buildHistoryFromLimit(limit);
            transactionLimitHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Error while save transaction limit history", e);
        }
    }

    // Helper methods

    private void checkLimitNotExists(String accountNo, String serviceType) {
        transactionLimitRepository
                .findActiveByAccountNoAndServiceType(accountNo, serviceType)
                .ifPresent(existing -> {
                    throw new BusinessException(String.format(
                            "Transaction limit already exists for accountNo: %s and serviceType: %s",
                            accountNo, serviceType));
                });
    }

    private void enrichRequestWithAccountDetails(TxnLimitRequest request) {
        AccountDetail.AccountDetailData accountDetail = cdpClient.getAccountInfo(request.getAccountNo());
        if (accountDetail == null) {
            throw new BusinessException(
                    String.format("Account detail not found for accountNo: %s", request.getAccountNo()));
        }
        if (StringUtils.hasText(accountDetail.getCustomerNo())) {
            request.setCustomerNo(accountDetail.getCustomerNo());
        }
    }

    private TransactionLimit findLimitForUpdate(TxnLimitRequest request) {
        if (StringUtils.hasText(request.getCustomerKey()) && StringUtils.hasText(request.getServiceType())) {
            return transactionLimitRepository
                    .findActiveByCustomerKeyAndServiceType(request.getCustomerKey(), request.getServiceType())
                    .orElse(null);
        }
        if (StringUtils.hasText(request.getAccountNo()) && StringUtils.hasText(request.getServiceType())) {
            return transactionLimitRepository
                    .findActiveByAccountNoAndServiceType(request.getAccountNo(), request.getServiceType())
                    .orElse(null);
        }
        if (request.getLimitSn() != null) {
            return transactionLimitRepository
                    .findById(request.getLimitSn())
                    .filter(l -> StatusType.ACTIVE.equals(l.getStatus()))
                    .orElse(null);
        }
        return null;
    }

    private void validateAndCheckDuplicates(TransactionLimit limit, TxnLimitRequest request) {
        String finalAccountNo =
                StringUtils.hasText(request.getAccountNo()) ? request.getAccountNo() : limit.getAccountNo();
        String finalServiceType =
                StringUtils.hasText(request.getServiceType()) ? request.getServiceType() : limit.getServiceType();

        boolean accountNoChanged = StringUtils.hasText(request.getAccountNo())
                && !limit.getAccountNo().equals(finalAccountNo);
        boolean serviceTypeChanged = StringUtils.hasText(request.getServiceType())
                && !limit.getServiceType().equals(finalServiceType);

        if (accountNoChanged || serviceTypeChanged) {
            transactionLimitRepository
                    .findActiveByAccountNoAndServiceType(finalAccountNo, finalServiceType)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(limit.getId())) {
                            throw new BusinessException(String.format(
                                    "Transaction limit already exists for accountNo: %s and serviceType: %s",
                                    finalAccountNo, finalServiceType));
                        }
                    });
        }
    }

    private void updateLimitFields(TransactionLimit limit, TxnLimitRequest request) {
        if (StringUtils.hasText(request.getServiceType())) {
            validateServiceType(request.getServiceType());
            limit.setServiceType(request.getServiceType());
        }
        if (StringUtils.hasText(request.getAccountNo())) {
            limit.setAccountNo(request.getAccountNo());
        }
        if (StringUtils.hasText(request.getCustomerKey())) {
            limit.setCustomerKey(request.getCustomerKey());
            String customerNo = extractCustomerNo(request.getCustomerKey());
            if (customerNo != null) {
                limit.setCustomerNo(customerNo);
            }
        }
        if (request.getDailyLimit() != null) {
            String serviceType =
                    StringUtils.hasText(request.getServiceType()) ? request.getServiceType() : limit.getServiceType();
            validateLimitAgainstThresholds(request.getDailyLimit(), serviceType);
            limit.setDailyLimit(request.getDailyLimit());
            limit.setMonthlyLimit(request.getDailyLimit().multiply(MONTHLY_LIMIT_MULTIPLIER));
            limit.setPerTransactionLimit(request.getDailyLimit());
        }
    }

    private List<TransactionLimit> findLimitsByRequest(TxnLimitRequest request) {
        if (StringUtils.hasText(request.getAccountNo())) {
            return transactionLimitRepository.findActiveByAccountNo(request.getAccountNo());
        }
        if (StringUtils.hasText(request.getCustomerKey())) {
            return transactionLimitRepository.findActiveByCustomerKey(request.getCustomerKey());
        }
        if (StringUtils.hasText(request.getMasterAccountNo())) {
            return transactionLimitRepository.findActiveByMasterAccountNo(request.getMasterAccountNo());
        }
        if (StringUtils.hasText(request.getCustomerNo())) {
            return transactionLimitRepository.findActiveByCustomerNo(request.getCustomerNo());
        }
        throw new BusinessException(
                "At least one of the following is required: accountNo, customerKey (customerKey), masterAccountNo, or customerNo");
    }

    private String buildCustomerKey(String customerNo, String accountNo, String customerKey) {
        if (StringUtils.hasText(customerNo) && StringUtils.hasText(accountNo)) {
            return customerNo + "_" + accountNo;
        }
        return customerKey;
    }

    private TransactionLimitHistory buildHistoryFromLimit(TransactionLimit limit) {
        return TransactionLimitHistory.builder()
                .transactionLimitSn(limit.getId())
                .customerKey(limit.getCustomerKey())
                .accountNo(limit.getAccountNo())
                .customerNo(limit.getCustomerNo())
                .masterAccountId(limit.getMasterAccountId())
                .masterAccountNo(limit.getMasterAccountNo())
                .channelCode(limit.getChannelCode())
                .serviceType(limit.getServiceType())
                .accountType(limit.getAccountType())
                .dailyLimit(limit.getDailyLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .perTransactionLimit(limit.getPerTransactionLimit())
                .dailyTransactionCount(limit.getDailyTransactionCount())
                .monthlyTransactionCount(limit.getMonthlyTransactionCount())
                .data(limit.getData())
                .metadata(limit.getMetadata())
                .status(limit.getStatus())
                .build();
    }

    /**
     * Validates that the provided daily limit falls within the system-defined
     * threshold ranges.
     *
     * @param dailyLimit
     *            the daily limit to validate
     * @param serviceType
     *            the service type to filter thresholds
     */
    private void validateLimitAgainstThresholds(BigDecimal dailyLimit, String serviceType) {
        if (dailyLimit == null || dailyLimit.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Find active thresholds that apply to daily limits, optionally filtered by
        // service type
        List<LimitThreshold> dailyThresholds;
        if (StringUtils.hasText(serviceType)) {
            // Find thresholds that match the service type OR are general (null serviceType)
            // This allows service-specific thresholds to override general ones
            dailyThresholds = limitThresholdRepository.findActiveByServiceTypeOrNullAndAmountTypes(
                    serviceType, DAILY_AMOUNT_TYPE, DAILY_AMOUNT_TYPE);
        } else {
            // If no service type provided, get all daily thresholds (including general
            // ones)
            dailyThresholds = limitThresholdRepository.findActiveByAmountTypes(DAILY_AMOUNT_TYPE, DAILY_AMOUNT_TYPE);
        }

        if (dailyThresholds.isEmpty()) {
            log.debug(
                    "No active daily limit thresholds found for serviceType: {}. Skipping threshold validation.",
                    serviceType);
            return; // No thresholds defined, allow any value
        }

        // Check if the daily limit falls within any threshold range
        boolean isValid = dailyThresholds.stream().anyMatch(threshold -> {
            BigDecimal minAmount = threshold.getMinAmount();
            BigDecimal maxAmount = threshold.getMaxAmount();

            boolean withinRange = dailyLimit.compareTo(minAmount) >= 0 && dailyLimit.compareTo(maxAmount) <= 0;

            if (withinRange) {
                log.debug(
                        "Daily limit {} is within threshold range [{}, {}] for serviceType: {}",
                        dailyLimit,
                        minAmount,
                        maxAmount,
                        threshold.getServiceType());
            }

            return withinRange;
        });

        if (!isValid) {
            // Find the overall min and max from all thresholds for error message
            BigDecimal overallMin = dailyThresholds.stream()
                    .map(LimitThreshold::getMinAmount)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal overallMax = dailyThresholds.stream()
                    .map(LimitThreshold::getMaxAmount)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            throw new BusinessException(String.format(
                    "Daily limit %s is outside the allowed range. Please set a limit between %s and %s (inclusive).",
                    dailyLimit, overallMin, overallMax));
        }
    }
}
