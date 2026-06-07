package com.example.persona.profile.service;

import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.profile.dto.request.CardAppManagementRequest;
import com.example.persona.profile.dto.response.CardAppProfileResponse;
import com.example.persona.profile.model.CardAppProfile;
import com.example.persona.profile.repository.CardAppProfileRepository;
import com.example.persona.utils.NullableDisplayUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class CardAppProfileService {

    private final CardAppProfileRepository cardAppProfileRepository;
    private final ProfileVersioningService versioningService;

    public CardAppProfileService(
            CardAppProfileRepository cardAppProfileRepository, ProfileVersioningService versioningService) {
        this.cardAppProfileRepository = cardAppProfileRepository;
        this.versioningService = versioningService;
    }

    @Transactional(readOnly = true)
    public CardAppProfileResponse getCardProfile(String customerNo, String accountNo, String cardNo) {
        log.debug("Fetching card app profile for customer: {}, account: {}, card: {}", customerNo, accountNo, cardNo);

        return null;
    }

    @Transactional(readOnly = true)
    public List<CardAppProfileResponse> getAllCardProfiles(String customerNo) {
        log.debug("Fetching all card app profiles for customer: {}", customerNo);

        return cardAppProfileRepository
                .findByCustomerNoAndStatusOrderByDisplayOrderAsc(customerNo, StatusType.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CardAppProfileResponse> getVisibleCardProfiles(String customerNo) {
        log.debug("Fetching visible card app profiles for customer: {}", customerNo);

        return cardAppProfileRepository
                .findByCustomerNoAndIsHiddenFalseAndStatusOrderByDisplayOrderAsc(customerNo, StatusType.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CardAppProfileResponse> getCardProfilesByAccount(String customerNo, String accountNo) {
        log.debug("Fetching card app profiles for customer: {}, account: {}", customerNo, accountNo);

        return cardAppProfileRepository
                .findByCustomerNoAndAccountNoAndStatusOrderByDisplayOrderAsc(customerNo, accountNo, StatusType.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CardAppProfileResponse createCardAppProfile(CardAppManagementRequest request) {
        log.debug("Creating card app profile for request: {}", request);

        // Validate required fields
        if (request.getCustomerNo() == null || request.getAccountNo() == null || request.getCardNo() == null) {
            throw new BusinessException(
                    "Customer number, account number, and card number are required to create card profile");
        }

        // Generate version key
        String versionKey = versioningService.generateCardVersionKey(
                request.getCustomerNo(), request.getAccountNo(), request.getCardNo());

        // Check if profile already exists
        cardAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .ifPresent(existing -> {
                    throw new BusinessException(String.format(
                            "Card app profile already exists for customer: %s, account: %s, card: %s",
                            request.getCustomerNo(), request.getAccountNo(), request.getCardNo()));
                });

        // Create new version (no old version for create)
        CardAppProfile newVersion = new CardAppProfile();
        versioningService.createNewVersion(null, newVersion, versionKey);

        // Set required fields
        newVersion.setCustomerNo(request.getCustomerNo());
        newVersion.setAccountNo(request.getAccountNo());
        newVersion.setTrackingNumber(request.getCardNo());

        // Set customer_app_id (required field) - use customerNo as default
        if (newVersion.getCustomerAppId() == null) {
            newVersion.setCustomerAppId(request.getCustomerNo());
        }

        // Set optional fields fromEntity request
        updateCardProfileFields(newVersion, request);
        handleDefaultPaymentCardUpdate(request, newVersion);

        // Save new version
        CardAppProfile savedProfile = cardAppProfileRepository.save(newVersion);
        log.info(
                "Card app profile created successfully for customer: {}, account: {}, card: {}",
                request.getCustomerNo(),
                request.getAccountNo(),
                request.getCardNo());

        return mapToResponse(savedProfile);
    }

    /**
     * Create or update card app profile. If an active profile exists for the
     * customer/account/card, updates it (new version); otherwise creates a new
     * profile.
     *
     * @param request
     *            the card app management request (customerNo, accountNo, cardNo
     *            required)
     * @return the saved card app profile response
     */
    @Transactional
    public CardAppProfileResponse createOrUpdateCardAppProfile(CardAppManagementRequest request) {
        log.debug("Create or update card app profile for request: {}", request);

        if (request.getCustomerNo() == null || request.getAccountNo() == null || request.getCardNo() == null) {
            throw new BusinessException("Customer number, account number, and card number are required");
        }

        String versionKey = versioningService.generateCardVersionKey(
                request.getCustomerNo(), request.getAccountNo(), request.getCardNo());

        return cardAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .map(existing -> updateCardManagement(request))
                .orElseGet(() -> createCardProfileForCreateOrUpdate(request, versionKey));
    }

    /**
     * Create or update multiple card app profiles in a single transaction.
     * Optimized for migration: one transaction, invalid requests skipped.
     *
     * @param requests
     *            list of card app management requests (customerNo, accountNo,
     *            cardNo required per item)
     */
    @Transactional
    public void createOrUpdateCardAppProfiles(List<CardAppManagementRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        requests.stream()
                .filter(req -> req.getCustomerNo() != null && req.getAccountNo() != null && req.getCardNo() != null)
                .forEach(this::createOrUpdateCardAppProfile);
    }

    /**
     * Creates a new card app profile (used when no active profile exists in
     * createOrUpdateCardAppProfile). Does not throw if profile already exists.
     */
    private CardAppProfileResponse createCardProfileForCreateOrUpdate(
            CardAppManagementRequest request, String versionKey) {
        CardAppProfile newVersion = new CardAppProfile();
        versioningService.createNewVersion(null, newVersion, versionKey);

        newVersion.setCustomerNo(request.getCustomerNo());
        newVersion.setAccountNo(request.getAccountNo());
        newVersion.setTrackingNumber(request.getCardTrackingNo());

        if (newVersion.getCustomerAppId() == null) {
            newVersion.setCustomerAppId(request.getCustomerNo());
        }

        updateCardProfileFields(newVersion, request);
        handleDefaultPaymentCardUpdate(request, newVersion);

        CardAppProfile savedProfile = cardAppProfileRepository.save(newVersion);
        log.info(
                "Card app profile created (createOrUpdate) for customer: {}, account: {}, card: {}",
                request.getCustomerNo(),
                request.getAccountNo(),
                request.getCardNo());
        return mapToResponse(savedProfile);
    }

    @Transactional
    public CardAppProfileResponse updateCardManagement(CardAppManagementRequest request) {
        log.debug("Updating card management for request: {}", request);

        // Generate version key
        String versionKey = versioningService.generateCardVersionKey(
                request.getCustomerNo(), request.getAccountNo(), request.getCardNo());

        // Find active version
        CardAppProfile oldVersion = cardAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        String.format("Card app profile not found for card: %s", request.getCardNo())));

        // Create new version (copies all fields fromEntity old)
        CardAppProfile newVersion = new CardAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);

        // Update only changed fields
        updateCardProfileFields(newVersion, request);
        handleDefaultPaymentCardUpdate(request, newVersion);

        // Save old version (DELETED) and new version (ACTIVE)
        cardAppProfileRepository.save(oldVersion);
        CardAppProfile savedProfile = cardAppProfileRepository.save(newVersion);
        log.info(
                "Card app profile version {} saved successfully for customer: {}, account: {}, card: {}",
                savedProfile.getVersion(),
                request.getCustomerNo(),
                request.getAccountNo(),
                request.getCardNo());

        return mapToResponse(savedProfile);
    }

    @Transactional
    public CardAppProfileResponse renameCard(String customerNo, String accountNo, String cardNo, String newCardName) {
        log.debug(
                "Renaming card for customer: {}, account: {}, card: {}, new name: {}",
                customerNo,
                accountNo,
                cardNo,
                newCardName);

        String versionKey = versioningService.generateCardVersionKey(customerNo, accountNo, cardNo);
        CardAppProfile oldVersion = cardAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(String.format(
                        "Card app profile not found for customer: %s, account: %s, card: %s",
                        customerNo, accountNo, cardNo)));

        CardAppProfile newVersion = new CardAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);
        newVersion.setCardName(newCardName);

        cardAppProfileRepository.save(oldVersion);
        CardAppProfile savedProfile = cardAppProfileRepository.save(newVersion);

        return mapToResponse(savedProfile);
    }

    @Transactional
    public CardAppProfileResponse updateTransactionMaxLimit(
            String customerNo, String accountNo, String cardNo, java.math.BigDecimal transactionMaxLimit) {
        log.debug(
                "Updating transaction max limit for customer: {}, account: {}, card: {}",
                customerNo,
                accountNo,
                cardNo);

        String versionKey = versioningService.generateCardVersionKey(customerNo, accountNo, cardNo);
        CardAppProfile oldVersion = cardAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(String.format(
                        "Card app profile not found for customer: %s, account: %s, card: %s",
                        customerNo, accountNo, cardNo)));

        CardAppProfile newVersion = new CardAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);
        newVersion.setTransactionMaxLimit(transactionMaxLimit);

        cardAppProfileRepository.save(oldVersion);
        CardAppProfile savedProfile = cardAppProfileRepository.save(newVersion);

        return mapToResponse(savedProfile);
    }

    @Transactional
    public CardAppProfileResponse hideCard(String customerNo, String accountNo, String cardNo) {
        log.debug("Hiding card for customer: {}, account: {}, card: {}", customerNo, accountNo, cardNo);

        String versionKey = versioningService.generateCardVersionKey(customerNo, accountNo, cardNo);
        CardAppProfile cardAppProfile = cardAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(String.format(
                        "Card app profile not found for customer: %s, account: %s, card: %s",
                        customerNo, accountNo, cardNo)));

        cardAppProfile.setIsHidden(true);
        cardAppProfileRepository.save(cardAppProfile);

        return mapToResponse(cardAppProfile);
    }

    @Transactional
    public CardAppProfileResponse showCard(String customerNo, String accountNo, String cardNo) {
        log.debug("Showing card for customer: {}, account: {}, card: {}", customerNo, accountNo, cardNo);

        String versionKey = versioningService.generateCardVersionKey(customerNo, accountNo, cardNo);
        CardAppProfile cardAppProfile = cardAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(String.format(
                        "Card app profile not found for customer: %s, account: %s, card: %s",
                        customerNo, accountNo, cardNo)));

        cardAppProfile.setIsHidden(false);
        cardAppProfileRepository.save(cardAppProfile);

        return mapToResponse(cardAppProfile);
    }

    @Transactional
    public CardAppProfileResponse setAsDefaultPaymentCard(String customerNo, String accountNo, String cardNo) {
        log.debug(
                "Setting card as default payment card for customer: {}, account: {}, card: {}",
                customerNo,
                accountNo,
                cardNo);

        String versionKey = versioningService.generateCardVersionKey(customerNo, accountNo, cardNo);
        CardAppProfile oldVersion = cardAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(String.format(
                        "Card app profile not found for customer: %s, account: %s, card: %s",
                        customerNo, accountNo, cardNo)));

        // Unset other cards as default
        List<CardAppProfile> allCards =
                cardAppProfileRepository.findByCustomerNoAndStatusOrderByDisplayOrderAsc(customerNo, StatusType.ACTIVE);
        List<CardAppProfile> cardsToUnset = allCards.stream()
                .filter(card ->
                        !card.getTrackingNumber().equals(cardNo) && Boolean.TRUE.equals(card.getIsDefaultPaymentCard()))
                .toList();
        batchUnsetDefaultPaymentCard(cardsToUnset);

        CardAppProfile newVersion = new CardAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);
        newVersion.setIsDefaultPaymentCard(true);

        cardAppProfileRepository.save(oldVersion);
        CardAppProfile savedProfile = cardAppProfileRepository.save(newVersion);

        return mapToResponse(savedProfile);
    }

    private void updateCardProfileFields(CardAppProfile newVersion, CardAppManagementRequest request) {
        Optional.ofNullable(request.getCardName()).ifPresent(newVersion::setCardName);
        Optional.ofNullable(request.getTransactionMaxLimit()).ifPresent(newVersion::setTransactionMaxLimit);
        Optional.ofNullable(request.getDailyLimit()).ifPresent(newVersion::setDailyLimit);
        Optional.ofNullable(request.getMonthlyLimit()).ifPresent(newVersion::setMonthlyLimit);
        Optional.ofNullable(request.getDisplayOrder()).ifPresent(newVersion::setDisplayOrder);
        Optional.ofNullable(request.getIsPinned()).ifPresent(newVersion::setIsPinned);
        Optional.ofNullable(request.getIsHidden()).ifPresent(newVersion::setIsHidden);
        Optional.ofNullable(request.getStopTransactionNotification())
                .ifPresent(newVersion::setStopTransactionNotification);
        Optional.ofNullable(request.getAllowTransaction()).ifPresent(newVersion::setAllowTransaction);
        Optional.ofNullable(request.getAllowOnlineTransaction()).ifPresent(newVersion::setAllowOnlineTransaction);
        Optional.ofNullable(request.getAllowContactlessTransaction())
                .ifPresent(newVersion::setAllowContactlessTransaction);
        Optional.ofNullable(request.getAllowInternationalTransaction())
                .ifPresent(newVersion::setAllowInternationalTransaction);
        Optional.ofNullable(request.getCardIcon()).ifPresent(newVersion::setCardIcon);
        Optional.ofNullable(request.getCardColor()).ifPresent(newVersion::setCardColor);
        Optional.ofNullable(request.getCardBrand()).ifPresent(newVersion::setCardBrand);
        Optional.ofNullable(request.getCardType()).ifPresent(newVersion::setCardType);
        Optional.ofNullable(request.getCardStatus()).ifPresent(newVersion::setCardStatus);
        Optional.ofNullable(request.getExpiryDate())
                .map(CardAppProfileService::parseCardExpiryString)
                .ifPresent(newVersion::setExpiryDate);
        Optional.ofNullable(request.getCardHolderName()).ifPresent(newVersion::setCardHolderName);
        Optional.ofNullable(request.getCssNumber()).ifPresent(newVersion::setCssNumber);
    }

    private void handleDefaultPaymentCardUpdate(CardAppManagementRequest request, CardAppProfile newVersion) {
        if (request.getIsDefaultPaymentCard() == null) {
            return;
        }

        boolean isDefaultPaymentCard = request.getIsDefaultPaymentCard();
        if (isDefaultPaymentCard) {
            unsetOtherCardsAsDefault(request.getCustomerNo(), request.getCardNo());
        }
        newVersion.setIsDefaultPaymentCard(request.getIsDefaultPaymentCard());
    }

    private void unsetOtherCardsAsDefault(String customerNo, String currentCardNo) {
        List<CardAppProfile> allCards =
                cardAppProfileRepository.findByCustomerNoAndStatusOrderByDisplayOrderAsc(customerNo, StatusType.ACTIVE);
        List<CardAppProfile> cardsToUnset = allCards.stream()
                .filter(card -> !card.getTrackingNumber().equals(currentCardNo))
                .toList();
        batchUnsetDefaultPaymentCard(cardsToUnset);
    }

    private void batchUnsetDefaultPaymentCard(List<CardAppProfile> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }

        List<CardAppProfile> allProfilesToSave = new java.util.ArrayList<>(cards.size() * 2);

        for (CardAppProfile card : cards) {
            CardAppProfile unsetNewVersion = new CardAppProfile();
            versioningService.createNewVersion(card, unsetNewVersion, card.getVersionKey());
            unsetNewVersion.setIsDefaultPaymentCard(false);
            allProfilesToSave.add(card);
            allProfilesToSave.add(unsetNewVersion);
        }

        cardAppProfileRepository.saveAll(allProfilesToSave);
    }

    /**
     * Parses card expiry strings from APIs, Oracle, or legacy {@code MM/dd} input.
     */
    static LocalDate parseCardExpiryString(String expiryDate) {
        if (!StringUtils.hasText(expiryDate)) {
            return null;
        }
        String s = expiryDate.trim();
        try {
            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        if (s.length() == 10) {
            for (DateTimeFormatter f : List.of(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"))) {
                try {
                    return LocalDate.parse(s, f);
                } catch (DateTimeParseException ignored) {
                    // try next
                }
            }
        }
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("MM/dd")
                    .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
                    .toFormatter();
            return LocalDate.parse(s, formatter);
        } catch (DateTimeParseException e) {
            log.debug("Unparseable card expiry: {} - {}", expiryDate, e.getMessage());
            return null;
        }
    }

    static String lastFourDigitsFromPan(String trackingNumber) {
        if (!StringUtils.hasText(trackingNumber)) {
            return null;
        }
        String digits = trackingNumber.replaceAll("\\D", "");
        if (digits.length() < 4) {
            return null;
        }
        return digits.substring(digits.length() - 4);
    }

    private CardAppProfileResponse mapToResponse(CardAppProfile profile) {
        String tracking = profile.getTrackingNumber();
        return CardAppProfileResponse.builder()
                .cardNo(NullableDisplayUtils.stringOrNa(tracking))
                .accountNo(NullableDisplayUtils.stringOrNa(profile.getAccountNo()))
                .trackingNo(NullableDisplayUtils.stringOrNa(tracking))
                .cardLastFour(NullableDisplayUtils.stringOrNa(lastFourDigitsFromPan(tracking)))
                .cardType(NullableDisplayUtils.stringOrNa(profile.getCardType()))
                .cardBrand(NullableDisplayUtils.stringOrNa(profile.getCardBrand()))
                .cardName(NullableDisplayUtils.stringOrNa(profile.getCardName()))
                .cardStatus(NullableDisplayUtils.stringOrNa(profile.getCardStatus()))
                .expiryDate(profile.getExpiryDate())
                .cardHolderName(NullableDisplayUtils.stringOrNa(profile.getCardHolderName()))
                .cssNumber(NullableDisplayUtils.stringOrNa(profile.getCssNumber()))
                .creditLimit(profile.getCreditLimit())
                .availableCredit(profile.getAvailableCredit())
                .transactionMaxLimit(profile.getTransactionMaxLimit())
                .dailyLimit(profile.getDailyLimit())
                .monthlyLimit(profile.getMonthlyLimit())
                .displayOrder(profile.getDisplayOrder())
                .isPinned(profile.getIsPinned())
                .isHidden(profile.getIsHidden())
                .stopTransactionNotification(profile.getStopTransactionNotification())
                .isDefaultPaymentCard(profile.getIsDefaultPaymentCard())
                .allowTransaction(profile.getAllowTransaction())
                .allowOnlineTransaction(profile.getAllowOnlineTransaction())
                .allowContactlessTransaction(profile.getAllowContactlessTransaction())
                .allowInternationalTransaction(profile.getAllowInternationalTransaction())
                .cardIcon(NullableDisplayUtils.stringOrNa(profile.getCardIcon()))
                .cardColor(NullableDisplayUtils.stringOrNa(profile.getCardColor()))
                .build();
    }
}
