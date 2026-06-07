package com.example.persona.migration.handler;

import com.example.persona.migration.dto.MigrationResult;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.model.oracle.MtxUserPhysicalCard;
import com.example.persona.migration.model.oracle.MtxVirtualCard;
import com.example.persona.migration.repository.oracle.MtxUserPhysicalCardRepository;
import com.example.persona.migration.repository.oracle.MtxVirtualCardRepository;
import com.example.persona.migration.util.CustomerKeyParser;
import com.example.persona.profile.dto.request.CardAppManagementRequest;
import com.example.persona.profile.model.CustomerProfile;
import com.example.persona.profile.repository.CustomerProfileRepository;
import com.example.persona.profile.service.CardAppProfileService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardMigrationHandler implements MigrationStageHandler {

    public static final String STAGE_CODE = "CARD";

    private final MtxVirtualCardRepository mtxVirtualCardRepository;
    private final MtxUserPhysicalCardRepository mtxUserPhysicalCardRepository;
    private final CardAppProfileService cardAppProfileService;
    private final CustomerProfileRepository customerProfileRepository;

    @Override
    public String getStageCode() {
        return STAGE_CODE;
    }

    @Override
    public MigrationResult execute(CustomerMigrationStage customerMigrationStage) {
        String customerKey = customerMigrationStage != null ? customerMigrationStage.getCustomerKey() : null;
        log.info("Starting card migration for customer: {}", customerKey);
        try {
            CustomerKeyParser.ParsedKey parsedKey = CustomerKeyParser.parse(
                    StringUtils.hasText(customerKey) ? customerKey : "", CustomerKeyParser.LoginIdStrategy.ACCOUNT_NO);
            if (parsedKey.accountNo() == null || parsedKey.customerNo() == null) {
                log.warn("CARD migration skipped: missing accountNo or customerNo in key: {}", customerKey);
                return MigrationResult.completed();
            }

            List<MtxVirtualCard> virtualCards =
                    nullToEmpty(mtxVirtualCardRepository.findAllByAccountNo(parsedKey.accountNo()));
            List<MtxUserPhysicalCard> physicalCards =
                    nullToEmpty(mtxUserPhysicalCardRepository.findAllByAccountNo(parsedKey.accountNo()));

            int virtualSize = virtualCards.size();
            int physicalSize = physicalCards.size();
            List<CardAppManagementRequest> requests = new ArrayList<>(virtualSize + physicalSize);
            AtomicInteger displayOrder = new AtomicInteger(0);
            String cardHolderName = resolveCardHolderName(parsedKey.customerNo());

            for (MtxVirtualCard card : virtualCards) {
                CardAppManagementRequest req = buildRequestFromVirtualCard(
                        parsedKey.customerNo(), parsedKey.accountNo(), card, displayOrder, cardHolderName);
                if (req != null) {
                    requests.add(req);
                }
            }
            for (int i = 0; i < physicalSize; i++) {
                MtxUserPhysicalCard card = physicalCards.get(i);
                CardAppManagementRequest req = buildRequestFromPhysicalCard(
                        parsedKey.customerNo(), parsedKey.accountNo(), card, displayOrder, cardHolderName);
                if (req != null) {
                    requests.add(req);
                }
            }

            if (CollectionUtils.isEmpty(requests)) {
                log.warn("Card migration skipped: empty info for customer: {}", customerKey);
                return MigrationResult.completed();
            }

            cardAppProfileService.createOrUpdateCardAppProfiles(requests);

            log.info("Card migration completed for customer: {} ({} card(s))", customerKey, requests.size());
            return MigrationResult.completed();

        } catch (Exception e) {
            log.error("CARD migration failed for customer: {}", customerKey, e);
            return MigrationResult.failure("CARD_ERROR", e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list != null ? list : List.of();
    }

    private String resolveCardHolderName(String customerNo) {
        if (!StringUtils.hasText(customerNo)) {
            return null;
        }
        return customerProfileRepository
                .findByCustomerNo(customerNo)
                .map(CustomerProfile::getCustomerName)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private CardAppManagementRequest buildRequestFromVirtualCard(
            String customerNo,
            String accountNo,
            MtxVirtualCard card,
            AtomicInteger displayOrder,
            String cardHolderName) {
        if (card == null || !StringUtils.hasText(card.getTrackingNumber())) {
            return null;
        }
        return CardAppManagementRequest.builder()
                .customerNo(customerNo)
                .accountNo(accountNo)
                .cardNo(card.getCardNumber())
                .cardTrackingNo(card.getTrackingNumber())
                .dailyLimit(card.getDlyPurLim())
                .transactionMaxLimit(card.getMaxDlyPurAmt())
                .phoneNo(card.getPhoneNumber())
                .cardType(card.getCardType())
                .cardBrand(inferCardBrandFromType(card.getCardType()))
                .cardStatus(card.getCardStatus())
                .expiryDate(card.getExpiryDate())
                .createdOn(card.getCreatedOn())
                .displayOrder(displayOrder.getAndIncrement())
                .isPinned(false)
                .isHidden(false)
                .stopTransactionNotification(false)
                .isDefaultPaymentCard(false)
                .allowTransaction(true)
                .allowOnlineTransaction(true)
                .allowContactlessTransaction(true)
                .allowInternationalTransaction(true)
                .cardHolderName(cardHolderName)
                .build();
    }

    private CardAppManagementRequest buildRequestFromPhysicalCard(
            String customerNo,
            String accountNo,
            MtxUserPhysicalCard card,
            AtomicInteger displayOrder,
            String cardHolderName) {
        if (card == null || !StringUtils.hasText(card.getTrackingNumber())) {
            return null;
        }
        return CardAppManagementRequest.builder()
                .customerNo(customerNo)
                .accountNo(accountNo)
                .cardTrackingNo(card.getTrackingNumber())
                .cardNo(card.getCardNumber())
                .cardType(card.getCardType())
                .cardBrand(inferCardBrandFromType(card.getCardType()))
                .dailyLimit(card.getDlyPurLim())
                .transactionMaxLimit(card.getMaxDlyPurAmt())
                .phoneNo(card.getPhoneNumber())
                .expiryDate(card.getExpiryDate())
                .cardStatus(card.getCardStatus())
                .displayOrder(displayOrder.getAndIncrement())
                .isPinned(false)
                .isHidden(false)
                .stopTransactionNotification(false)
                .isDefaultPaymentCard(false)
                .allowTransaction(true)
                .allowOnlineTransaction(true)
                .allowContactlessTransaction(true)
                .allowInternationalTransaction(true)
                .cardHolderName(cardHolderName)
                .build();
    }

    /**
     * Maps Oracle {@code CARD_TYPE} text to a display brand (falls back to the raw type).
     */
    static String inferCardBrandFromType(String cardType) {
        if (!StringUtils.hasText(cardType)) {
            return null;
        }
        String u = cardType.trim().toUpperCase();
        if (u.contains("VISA")) {
            return "VISA";
        }
        if (u.contains("MASTER") || u.equals("MC")) {
            return "MASTERCARD";
        }
        if (u.contains("UNION")) {
            return "UNIONPAY";
        }
        if (u.contains("JCB")) {
            return "JCB";
        }
        return cardType.trim();
    }
}
