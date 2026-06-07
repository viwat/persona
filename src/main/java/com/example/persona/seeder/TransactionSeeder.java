package com.example.persona.seeder;

import com.example.persona.enums.AccountType;
import com.example.persona.enums.StatusType;
import com.example.persona.transaction.model.AuthenticationMethod;
import com.example.persona.transaction.model.AuthenticationThreshold;
import com.example.persona.transaction.model.PaymentChannel;
import com.example.persona.transaction.model.TransactionAuthorization;
import com.example.persona.transaction.model.TransactionDetail;
import com.example.persona.transaction.model.TransactionLimit;
import com.example.persona.transaction.repository.AuthenticationThresholdRepository;
import com.example.persona.transaction.repository.PaymentChannelRepository;
import com.example.persona.transaction.repository.TransactionAuthorizationRepository;
import com.example.persona.transaction.repository.TransactionDetailRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TransactionSeeder {

    private final TransactionAuthorizationRepository transactionAuthorizationRepository;
    private final PaymentChannelRepository paymentChannelRepository;
    private final AuthenticationThresholdRepository authenticationThresholdRepository;
    private final TransactionDetailRepository transactionDetailRepository;

    @Bean
    @Profile("seed")
    public CommandLineRunner seedTransactionAuthorizations() {
        return args -> {
            log.info("Starting transaction authorizations seeding...");

            if (transactionAuthorizationRepository.count() > 0) {
                log.info("Transaction authorizations already exist, skipping...");
                return;
            }

            log.info("Setting up payment channels and authentication thresholds...");
            // Fetch required relationships
            PaymentChannel mobileChannel = paymentChannelRepository
                    .findByChannelCode("MOBILE")
                    .orElseGet(() -> {
                        PaymentChannel newChannel = new PaymentChannel();
                        newChannel.setChannelCode("MOBILE");
                        newChannel.setChannelName("Mobile Channel");
                        newChannel.setDescription("Mobile banking channel");
                        return paymentChannelRepository.save(newChannel);
                    });
            AuthenticationThreshold standardThreshold = authenticationThresholdRepository
                    .findByAuthenticationMethod(AuthenticationMethod.FACE_PASS)
                    .orElseGet(() -> {
                        AuthenticationThreshold newThreshold = new AuthenticationThreshold();
                        newThreshold.setAuthenticationMethod(AuthenticationMethod.FACE_PASS);
                        newThreshold.setMinimumAmount(new BigDecimal("1000.00"));
                        newThreshold.setMaximumAmount(new BigDecimal("10000.00"));
                        return authenticationThresholdRepository.save(newThreshold);
                    });

            List<TransactionAuthorization> authorizations = Arrays.asList(
                    createTransactionAuthorization(
                            "KEY123",
                            "1234567890",
                            "C123456",
                            "MASTER123",
                            "MASTER123",
                            "TRANSFER1",
                            mobileChannel,
                            standardThreshold,
                            new BigDecimal("1000.00"),
                            new BigDecimal("10000.00"),
                            "{\"currency\": \"IDR\", \"authType\": \"OTP\"}",
                            "{\"lastUpdate\": \"2024-03-20\", \"securityLevel\": \"HIGH\"}"),
                    createTransactionAuthorization(
                            "KEY456",
                            "9876543210",
                            "C789012",
                            "MASTER456",
                            "MASTER456",
                            "TRANSFER2",
                            mobileChannel,
                            standardThreshold,
                            new BigDecimal("5000.00"),
                            new BigDecimal("50000.00"),
                            "{\"currency\": \"IDR\", \"authType\": \"BIOMETRIC\"}",
                            "{\"lastUpdate\": \"2024-03-20\", \"securityLevel\": \"VERY_HIGH\"}"),
                    createTransactionAuthorization(
                            "KEY789",
                            "5678901234",
                            "C345678",
                            "MASTER789",
                            "MASTER789",
                            "TRANSFER3",
                            mobileChannel,
                            standardThreshold,
                            new BigDecimal("500.00"),
                            new BigDecimal("5000.00"),
                            "{\"currency\": \"IDR\", \"authType\": \"PIN\"}",
                            "{\"lastUpdate\": \"2024-03-19\", \"securityLevel\": \"MEDIUM\"}"));

            int successCount = 0;
            for (TransactionAuthorization auth : authorizations) {
                try {
                    transactionAuthorizationRepository.save(auth);
                    log.info(
                            "Seeded transaction authorization: {} - Customer: {} (Min: {}, Max: {})",
                            auth.getCustomerKey(),
                            auth.getCustomerNo(),
                            auth.getMinAmount(),
                            auth.getMaxAmount());
                    successCount++;
                } catch (Exception e) {
                    log.error("Error seeding transaction authorization for customer: {}", auth.getCustomerNo(), e);
                }
            }
            log.info("Completed seeding {} transaction authorizations", successCount);
        };
    }

    @Bean
    @Profile("seed")
    public CommandLineRunner seedTransactionDetails() {
        return args -> {
            log.info("Starting transaction details seeding...");
            /*
             * if (transactionDetailRepository.count() > 0) {
             * log.info("Transaction details already exist, skipping..."); return; }
             *
             * List<TransactionDetail> details = Arrays.asList(
             * createTransactionDetail("1234567890", "C123456", "BR001", "TERM001",
             * "+621234567890", "DEV001", "MASTER123", "MASTER123", "BANK_TRANSFER",
             * "ONLINE_BANKING", "MOBILE", "AUTH001", "OTP", "Fund Transfer", "TRX001",
             * "REQ001", "SESS001", "TRX20240320001", "SUB001", "REF20240320001", new
             * BigDecimal("1000000.00"), "TRANSFER", "Regular transfer", null,
             * "{\"currency\": \"IDR\", \"status\": \"SUCCESS\"}"),
             * createTransactionDetail("9876543210", "C789012", "BR002", "TERM002",
             * "+621234567891", "DEV002", "MASTER456", "MASTER456", "E_WALLET",
             * "DIGITAL_PAYMENT", "MOBILE", "AUTH002", "BIOMETRIC", "Birthday Gift",
             * "TRX002", "REQ002", "SESS002", "TRX20240320002", "SUB002", "REF20240320002",
             * new BigDecimal("500000.00"), "GIFT", "Birthday gift transfer", "BIRTHDAY",
             * "{\"currency\": \"IDR\", \"status\": \"SUCCESS\"}"),
             * createTransactionDetail("5678901234", "C345678", "BR003", "TERM003",
             * "+621234567892", "DEV003", "MASTER789", "MASTER789", "INSTANT_TRANSFER",
             * "FAST_PAYMENT", "MOBILE", "AUTH003", "PIN", "Quick Transfer", "TRX003",
             * "REQ003", "SESS003", "TRX20240320003", "SUB003", "REF20240320003", new
             * BigDecimal("750000.00"), "TRANSFER", "Urgent transfer", null,
             * "{\"currency\": \"IDR\", \"status\": \"SUCCESS\"}"));
             *
             * int successCount = 0; for (TransactionDetail detail : details) { try { //
             * transactionDetailRepository.save(detail); log.
             * info("Seeded transaction detail: {} - Customer: {} (Amount: {}, Type: {})",
             * detail.getTransactionReference(), detail.getCustomerNo(), detail.getAmount(),
             * detail.getType()); successCount++; } catch (Exception e) {
             * log.error("Error seeding transaction detail with reference: {}",
             * detail.getTransactionReference(), e); } }
             * log.info("Completed seeding {} transaction details", successCount);
             */
        };
    }

    private TransactionLimit createTransactionLimit(
            String customerKey,
            String accountNo,
            String customerNo,
            String masterAccountId,
            String masterAccountNo,
            String serviceType,
            String channelCode,
            AccountType accountType,
            BigDecimal dailyLimit,
            BigDecimal monthlyLimit,
            BigDecimal perTransactionLimit,
            Integer dailyTransactionCount,
            Integer monthlyTransactionCount,
            String data,
            String metadata) {

        return TransactionLimit.builder()
                .customerKey(customerKey)
                .accountNo(accountNo)
                .customerNo(customerNo)
                .masterAccountId(masterAccountId)
                .masterAccountNo(masterAccountNo)
                .serviceType(serviceType)
                .channelCode(channelCode)
                .accountType(accountType)
                .dailyLimit(dailyLimit)
                .monthlyLimit(monthlyLimit)
                .perTransactionLimit(perTransactionLimit)
                .dailyTransactionCount(dailyTransactionCount)
                .monthlyTransactionCount(monthlyTransactionCount)
                .data(data)
                .metadata(metadata)
                .build();
    }

    private TransactionAuthorization createTransactionAuthorization(
            String customerKey,
            String accountNo,
            String customerNo,
            String masterAccountId,
            String masterAccountNo,
            String serviceType,
            PaymentChannel paymentChannel,
            AuthenticationThreshold authenticationThreshold,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String data,
            String metadata) {

        TransactionAuthorization auth = new TransactionAuthorization();
        auth.setCustomerKey(customerKey);
        auth.setAccountNo(accountNo);
        auth.setStatus(StatusType.ACTIVE);
        auth.setCustomerNo(customerNo);
        auth.setMasterAccountId(masterAccountId);
        auth.setMasterAccountNo(masterAccountNo);
        auth.setServiceType(serviceType);
        auth.setPaymentChannel(paymentChannel);
        auth.setAuthenticationThreshold(authenticationThreshold);
        auth.setMinAmount(minAmount);
        auth.setMaxAmount(maxAmount);
        auth.setMaxAmountType("MAX");
        auth.setData(data);
        auth.setMetadata(metadata);
        return auth;
    }

    private TransactionDetail createTransactionDetail(
            String accountNo,
            String customerNo,
            String branchCode,
            String terminalId,
            String phoneNo,
            String deviceId,
            String masterAccountId,
            String masterAccountNo,
            String paymentVia,
            String paymentMethod,
            String paymentChannel,
            String transactionAuthCode,
            String authMethod,
            String title,
            String traceId,
            String requestId,
            String sessionId,
            String transactionId,
            String subTransactionId,
            String transactionReference,
            BigDecimal amount,
            String type,
            String remark,
            String giftFormat,
            String metadata) {

        return TransactionDetail.builder()
                .accountNo(accountNo)
                .customerNo(customerNo)
                .branchCode(branchCode)
                .terminalId(terminalId)
                .phoneNo(phoneNo)
                .deviceId(deviceId)
                .masterAccountId(masterAccountId)
                .masterAccountNo(masterAccountNo)
                .paymentVia(paymentVia)
                .paymentMethod(paymentMethod)
                .paymentChannel(paymentChannel)
                .transactionAuthCode(transactionAuthCode)
                .authMethod(authMethod)
                .title(title)
                .traceId(traceId)
                .requestId(requestId)
                .sessionId(sessionId)
                .transactionId(transactionId)
                .subTransactionId(subTransactionId)
                .transactionReference(transactionReference)
                .amount(amount)
                .type(type)
                .remark(remark)
                .giftFormat(giftFormat)
                .metadata(metadata)
                .build();
    }
}
