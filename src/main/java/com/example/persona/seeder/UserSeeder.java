package com.example.persona.seeder;

import com.example.persona.model.MultilingualContent;
import com.example.persona.profile.model.AccountAppProfile;
import com.example.persona.profile.model.CustomerAppProfile;
import com.example.persona.profile.repository.AccountAppProfileRepository;
import com.example.persona.profile.repository.CustomerAppProfileRepository;
import com.example.persona.theme.model.UserPersona;
import com.example.persona.theme.repository.UserPersonaRepository;
import com.example.persona.widget.model.PersonaWidget;
import com.example.persona.widget.repository.PersonaWidgetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserSeeder {

    private final UserPersonaRepository userPersonaRepository;
    private final AccountAppProfileRepository accountAppProfileRepository;
    private final CustomerAppProfileRepository customerAppProfileRepository;
    private final PersonaWidgetRepository personaWidgetRepository;

    // @Bean
    @Profile("seed")
    public CommandLineRunner seedUserData() {
        return args -> {
            log.info("Starting user data seeding...");

            // Seed User Personas
            log.info("Seeding user personas...");
            if (userPersonaRepository.count() == 0) {
                List<UserPersona> personas = Arrays.asList(
                        createUserPersona(
                                "DEFAULT",
                                "12345",
                                "1234567890",
                                "+62812345678",
                                "MASTER123",
                                "KEY123",
                                "MOBILE",
                                "LIGHT",
                                "MODERN",
                                "#FF5733",
                                "default_icon",
                                "light",
                                "medium"),
                        createUserPersona(
                                "PREMIUM",
                                "67890",
                                "9876543210",
                                "+62898765432",
                                "MASTER456",
                                "KEY456",
                                "MOBILE",
                                "DARK",
                                "PREMIUM",
                                "#33FF57",
                                "premium_icon",
                                "dark",
                                "large"));

                int personaCount = 0;
                for (UserPersona persona : personas) {
                    try {
                        userPersonaRepository.save(persona);
                        log.info(
                                "Seeded user persona: {} - Customer: {} (Theme: {})",
                                persona.getPersonaCode(),
                                persona.getCustomerNo(),
                                persona.getThemeCode());
                        personaCount++;
                    } catch (Exception e) {
                        log.error("Error seeding user persona: {}", persona.getPersonaCode(), e);
                    }
                }
                log.info("Completed seeding {} user personas", personaCount);
            } else {
                log.info("User personas already exist, skipping...");
            }

            // Seed Account Profiles
            log.info("Seeding account profiles...");
            if (accountAppProfileRepository.count() == 0) {
                List<AccountAppProfile> accounts = Arrays.asList(
                        createAccountProfile(
                                "C123456",
                                "1234567890",
                                "SAVINGS",
                                "ACTIVE",
                                "Regular Savings",
                                "John Doe",
                                "PERSONAL",
                                "SAVINGS",
                                "REGULAR",
                                "SAV001",
                                "STANDARD",
                                "INDIVIDUAL",
                                "RETAIL",
                                "{\"lastAccess\": \"2024-03-20\"}",
                                new BigDecimal("5000.00"),
                                false,
                                null),
                        createAccountProfile(
                                "C789012",
                                "9876543210",
                                "CHECKING",
                                "ACTIVE",
                                "Premium Checking",
                                "Jane Smith",
                                "BUSINESS",
                                "CHECKING",
                                "PREMIUM",
                                "CHK001",
                                "PREMIUM",
                                "CORPORATE",
                                "PRIORITY",
                                "{\"lastAccess\": \"2024-03-20\"}",
                                new BigDecimal("50000.00"),
                                true,
                                "VIP Customer"),
                        createAccountProfile(
                                "C345678",
                                "5678901234",
                                "SAVINGS",
                                "DORMANT",
                                "Student Savings",
                                "Alex Johnson",
                                "PERSONAL",
                                "SAVINGS",
                                "STUDENT",
                                "SAV002",
                                "BASIC",
                                "INDIVIDUAL",
                                "MASS",
                                "{\"lastAccess\": \"2024-03-19\"}",
                                new BigDecimal("100.00"),
                                false,
                                null));

                int accountCount = 0;
                for (AccountAppProfile account : accounts) {
                    try {
                        accountAppProfileRepository.save(account);
                        log.info(
                                "Seeded account profile: {} - {} ({})",
                                account.getCustomerKey(),
                                account.getAccountNo(),
                                account.getAccountType());
                        accountCount++;
                    } catch (Exception e) {
                        log.error("Error seeding account profile: {}", account.getAccountNo(), e);
                    }
                }
                log.info("Completed seeding {} account profiles", accountCount);
            } else {
                log.info("Account profiles already exist, skipping...");
            }

            // Seed Customer Profiles
            log.info("Seeding customer profiles...");
            if (customerAppProfileRepository.count() == 0) {
                List<CustomerAppProfile> customers = Arrays.asList(
                        createCustomerProfile(
                                "C123456",
                                "John Doe",
                                "INDIVIDUAL",
                                "ACTIVE",
                                "RETAIL",
                                "MASS",
                                "REGULAR",
                                "SALARIED",
                                "PERSONAL",
                                "EMPLOYEE",
                                "VERIFIED",
                                LocalDate.now(),
                                "Complete verification",
                                "{\"lastUpdate\": \"2024-03-20\", \"riskLevel\": \"LOW\"}"),
                        createCustomerProfile(
                                "C789012",
                                "Jane Smith",
                                "CORPORATE",
                                "ACTIVE",
                                "PRIORITY",
                                "BUSINESS",
                                "PREMIUM",
                                "ENTERPRISE",
                                "BUSINESS",
                                "SME",
                                "VERIFIED",
                                LocalDate.now().minusMonths(3),
                                "Annual verification complete",
                                "{\"lastUpdate\": \"2024-03-20\", \"riskLevel\": \"MEDIUM\"}"),
                        createCustomerProfile(
                                "C345678",
                                "Alex Johnson",
                                "INDIVIDUAL",
                                "PENDING",
                                "MASS",
                                "YOUTH",
                                "STUDENT",
                                "UNIVERSITY",
                                "PERSONAL",
                                "STUDENT",
                                "PENDING",
                                LocalDate.now().minusDays(5),
                                "Awaiting document verification",
                                "{\"lastUpdate\": \"2024-03-19\", \"riskLevel\": \"LOW\"}"));

                int customerCount = 0;
                for (CustomerAppProfile customer : customers) {
                    try {
                        customerAppProfileRepository.save(customer);
                        log.info(
                                "Seeded customer profile: {} - {}",
                                customer.getCustomerNo(),
                                customer.getCustomerName());
                        customerCount++;
                    } catch (Exception e) {
                        log.error("Error seeding customer profile: {}", customer.getCustomerNo(), e);
                    }
                }
                log.info("Completed seeding {} customer profiles", customerCount);
            } else {
                log.info("Customer profiles already exist, skipping...");
            }

            // Seed Persona Widgets
            log.info("Seeding persona widgets...");
            if (personaWidgetRepository.count() == 0) {
                List<PersonaWidget> personaWidgets = Arrays.asList(
                        // Regular customer - Quick Transfer widget
                        createPersonaWidget(
                                "12345",
                                "1234567890",
                                "+62812345678",
                                "MASTER123",
                                "KEY123",
                                "MOBILE",
                                1L,
                                "TRANSFER",
                                1,
                                "amount",
                                "1000",
                                "currency",
                                "USD",
                                "recipient",
                                "JOHN_DOE",
                                null,
                                null,
                                null,
                                null),

                        // Premium customer - Investment Portfolio widget
                        createPersonaWidget(
                                "67890",
                                "9876543210",
                                "+62898765432",
                                "MASTER456",
                                "KEY456",
                                "MOBILE",
                                2L,
                                "INVESTMENT",
                                1,
                                "period",
                                "1Y",
                                "type",
                                "PORTFOLIO",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null),

                        // Student - Payment widget
                        createPersonaWidget(
                                "12345",
                                "5678901234",
                                "+62812345678",
                                "MASTER123",
                                "KEY123",
                                "MOBILE",
                                3L,
                                "PAYMENT",
                                2,
                                "amount",
                                "500",
                                "institution",
                                "UNIVERSITY_A",
                                "semester",
                                "FALL_2024",
                                null,
                                null,
                                null,
                                null));

                int widgetCount = 0;
                for (PersonaWidget widget : personaWidgets) {
                    try {
                        personaWidgetRepository.save(widget);
                        log.info(
                                "Seeded persona widget: {} - Customer: {} (Order: {})",
                                widget.getWidgetCode(),
                                widget.getCustomerNo(),
                                widget.getDisplayOrder());
                        widgetCount++;
                    } catch (Exception e) {
                        log.error(
                                "Error seeding persona widget: {} for customer: {}",
                                widget.getWidgetCode(),
                                widget.getCustomerNo(),
                                e);
                    }
                }
                log.info("Completed seeding {} persona widgets", widgetCount);
            } else {
                log.info("Persona widgets already exist, skipping...");
            }

            log.info("User data seeding completed successfully!");
        };
    }

    private UserPersona createUserPersona(
            String personaCode,
            String customerNo,
            String accountNo,
            String phoneNo,
            String masterAccountNo,
            String customerKey,
            String channelCode,
            String themeCode,
            String themeCategory,
            String accentColor,
            String appIcon,
            String appearance,
            String textSize) {
        UserPersona persona = new UserPersona();
        persona.setPersonaCode(personaCode);
        persona.setCustomerNo(customerNo);
        persona.setAccountNo(accountNo);
        persona.setPhoneNo(phoneNo);
        persona.setMasterAccountNo(masterAccountNo);
        persona.setCustomerKey(customerKey);
        persona.setChannelCode(channelCode);
        persona.setThemeCode(themeCode);
        persona.setThemeCategory(themeCategory);
        persona.setAccentColor(accentColor);
        persona.setAppIcon(appIcon);
        persona.setAppearance(appearance);
        persona.setTextSize(textSize);
        persona.setVersion(1);
        persona.setStatus(com.example.persona.enums.StatusType.ACTIVE);
        return persona;
    }

    private AccountAppProfile createAccountProfile(
            String customerNo,
            String accountNo,
            String accountType,
            String accountStatus,
            String accountName,
            String accountHolderName,
            String serviceType,
            String serviceSubType,
            String entryType,
            String serviceCode,
            String classOfService,
            String accountCategory,
            String customerSegment,
            String metaData,
            BigDecimal balance,
            Boolean waiveFee,
            String waiveFeeReason) {

        AccountAppProfile profile = new AccountAppProfile();
        profile.setCustomerKey(customerNo);
        profile.setCustomerNo(customerNo);
        profile.setAccountNo(accountNo);
        profile.setAccountType(accountType);
        profile.setAccountStatus(accountStatus);
        profile.setAccountName(accountName);
        profile.setAccountHolderName(accountHolderName);
        profile.setClassOfService(classOfService);
        profile.setAccountCategory(accountCategory);
        profile.setMetaData(metaData);
        profile.setStatus(com.example.persona.enums.StatusType.ACTIVE);
        profile.setVersionKey(customerNo + "_" + accountNo);
        profile.setAccountHash(profile.generateAccountHash());
        return profile;
    }

    private CustomerAppProfile createCustomerProfile(
            String customerNo,
            String customerName,
            String customerType,
            String customerStatus,
            String customerSegment,
            String customerGroup,
            String customerClass,
            String customerSubClass,
            String customerCategory,
            String customerSubCategory,
            String kycStatus,
            LocalDate kycStatusDate,
            String kycStatusReason,
            String metaData) {

        CustomerAppProfile profile = new CustomerAppProfile();
        profile.setCustomerAppId(customerNo);
        profile.setCustomerKey(customerNo);
        profile.setCustomerNo(customerNo);
        profile.setCustomerName(customerName);
        profile.setKycStatus(kycStatus);
        profile.setKycStatusDate(kycStatusDate);
        profile.setKycStatusReason(kycStatusReason);
        profile.setMetaData(metaData);
        profile.setStatus(com.example.persona.enums.StatusType.ACTIVE);
        profile.setVersionKey(customerNo);
        return profile;
    }

    private PersonaWidget createPersonaWidget(
            String customerNo,
            String accountNo,
            String phoneNo,
            String masterAccountNo,
            String customerKey,
            String channelCode,
            Long widgetId,
            String widgetCode,
            int displayOrder,
            String param1Key,
            String param1Value,
            String param2Key,
            String param2Value,
            String param3Key,
            String param3Value,
            String param4Key,
            String param4Value,
            String param5Key,
            String param5Value) {

        return new PersonaWidget(
                null, // sn will be auto-generated
                customerNo,
                accountNo,
                phoneNo,
                masterAccountNo,
                customerKey,
                channelCode,
                widgetId,
                widgetCode,
                displayOrder,
                param1Key,
                param1Value,
                param2Key,
                param2Value,
                param3Key,
                param3Value,
                param4Key,
                param4Value,
                param5Key,
                param5Value);
    }

    private MultilingualContent createMultilingualContent(String en, String km, String zh) {
        return MultilingualContent.builder().en(en).km(km).zh(zh).build();
    }
}
