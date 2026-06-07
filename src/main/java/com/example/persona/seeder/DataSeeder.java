package com.example.persona.seeder;

import com.example.persona.catalog.model.Product;
import com.example.persona.catalog.model.ProductCategory;
import com.example.persona.catalog.model.ProductSubcategory;
import com.example.persona.catalog.repository.ProductCategoryRepository;
import com.example.persona.catalog.repository.ProductRepository;
import com.example.persona.catalog.repository.ProductSubcategoryRepository;
import com.example.persona.enums.StatusType;
import com.example.persona.favorite.model.Favorite;
import com.example.persona.favorite.repository.FavoriteRepository;
import com.example.persona.home.model.Announcement;
import com.example.persona.home.model.Promotional;
import com.example.persona.home.repository.AnnouncementRepository;
import com.example.persona.home.repository.PromotionalRepository;
import com.example.persona.i18n.model.Translation;
import com.example.persona.i18n.repository.TranslationRepository;
import com.example.persona.misc.model.Enumeration;
import com.example.persona.misc.repository.EnumerationRepository;
import com.example.persona.model.MultilingualContent;
import com.example.persona.schedule.model.Schedule;
import com.example.persona.schedule.repository.ScheduleRepository;
import com.example.persona.setting.model.Setting;
import com.example.persona.setting.repository.SettingRepository;
import com.example.persona.utils.ObjectMapperUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {
    private final SettingRepository settingRepository;
    private final FavoriteRepository favoriteRepository;
    private final ScheduleRepository scheduleRepository;
    private final TranslationRepository translationRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSubcategoryRepository productSubcategoryRepository;
    private final ProductRepository productRepository;
    private final EnumerationRepository enumerationRepository;
    private final AnnouncementRepository announcementRepository;
    private final PromotionalRepository promotionalRepository;

    @Bean
    @Profile("seed")
    public CommandLineRunner seedData() {
        return args -> {
            log.info("Starting data seeding...");

            // Check if setting exists before creating
            if (settingRepository.count() == 0) {
                Setting setting = Setting.builder()
                        .code("app.transaction.search.enabled")
                        .settingKey("app.transaction.search.enabled")
                        .defaultValue("true")
                        .build();
                // settingRepository.save(setting);
                log.info("Created transaction search setting");
            } // Seed favorite data
            if (favoriteRepository.count() == 0) {
                List<Favorite> favorites = Arrays.asList(
                        createFavorite(
                                "TRANSFER",
                                "BANK_TRANSFER",
                                "Bank Transfer",
                                "Transfer money to other bank accounts",
                                "bank_transfer_icon"),
                        createFavorite(
                                "BILL_PAYMENT",
                                "ELECTRICITY_BILL",
                                "Electricity Bill",
                                "Pay your electricity bills",
                                "electricity_bill_icon"),
                        createFavorite(
                                "TOP_UP",
                                "MOBILE_CREDIT",
                                "Mobile Top Up",
                                "Recharge your mobile credit",
                                "mobile_topup_icon"));

                favorites.forEach(favorite -> {
                    try {
                        favoriteRepository.save(favorite);
                        log.info("Seeded favorite: {}", favorite.getServiceCode());
                    } catch (Exception e) {
                        log.error("Error seeding favorite", e);
                    }
                });
            }

            // Seed schedule data
            if (scheduleRepository.count() == 0) {
                Map<String, Object> schedule1 = new HashMap<>();
                schedule1.put("data", List.of("accountNumber", "amount", "description"));
                Map<String, Object> schedule2 = new HashMap<>();
                schedule2.put("data", List.of("billNumber", "amount"));
                Map<String, Object> schedule3 = new HashMap<>();
                schedule3.put("data", List.of("phoneNumber", "amount"));
                List<Schedule> schedules = Arrays.asList(
                        createSchedule(
                                "TRANSFER",
                                "BANK_TRANSFER",
                                "MONTHLY",
                                "Monthly Bank Transfer",
                                "Schedule monthly transfers to other accounts",
                                "bank_transfer_icon",
                                schedule1,
                                BigDecimal.valueOf(10000.0),
                                BigDecimal.valueOf(10.0),
                                5),
                        createSchedule(
                                "BILL_PAYMENT",
                                "ELECTRICITY_BILL",
                                "MONTHLY",
                                "Monthly Electricity Bill",
                                "Schedule monthly electricity bill payments",
                                "electricity_bill_icon",
                                schedule2,
                                BigDecimal.valueOf(5000.0),
                                BigDecimal.valueOf(1.0),
                                3),
                        createSchedule(
                                "TOP_UP",
                                "MOBILE_CREDIT",
                                "WEEKLY",
                                "Weekly Mobile Top Up",
                                "Schedule weekly mobile credit recharges",
                                "mobile_topup_icon",
                                schedule3,
                                BigDecimal.valueOf(1000.0),
                                BigDecimal.valueOf(5.0),
                                10));

                schedules.forEach(schedule -> {
                    try {
                        scheduleRepository.save(schedule);
                        log.info("Seeded schedule: {}", schedule.getServiceCode());
                    } catch (Exception e) {
                        log.error("Error seeding schedule", e);
                    }
                });
            }

            // Seed translation data
            if (translationRepository.count() == 0) {
                List<Translation> translations = Arrays.asList(
                        createTranslation(
                                "TRANSFER_TITLE",
                                "PERSONA",
                                "TRANSFER",
                                "BANK_TRANSFER",
                                "DISPLAY",
                                MultilingualContent.builder()
                                        .en("Bank Transfer")
                                        .km("ផ្ទេរប្រាក់")
                                        .zh("银行转账")
                                        .build(),
                                "Translation for bank transfer title"),
                        createTranslation(
                                "BILL_PAYMENT_TITLE",
                                "PERSONA",
                                "PAYMENT",
                                "ELECTRICITY",
                                "DISPLAY",
                                MultilingualContent.builder()
                                        .en("Electricity Bill Payment")
                                        .km("ការបង់វិក្កយបត្រអគ្គិសនី")
                                        .zh("电费支付")
                                        .build(),
                                "Translation for electricity bill payment"),
                        createTranslation(
                                "MOBILE_TOPUP_TITLE",
                                "PERSONA",
                                "TOP_UP",
                                "MOBILE",
                                "DISPLAY",
                                MultilingualContent.builder()
                                        .en("Mobile Top Up")
                                        .km("បញ្ចូលទឹកប្រាក់ទូរស័ព្ទ")
                                        .zh("手机充值")
                                        .build(),
                                "Translation for mobile top up"),
                        // Greeting translations for /home/greeting (locale from User-Lang header)
                        createTranslation(
                                "GREETING_MORNING",
                                "PERSONA",
                                "HOME",
                                "GREETING",
                                "DISPLAY",
                                MultilingualContent.builder()
                                        .en("Good Morning")
                                        .km("អរុណសួស្តី")
                                        .zh("早上好")
                                        .build(),
                                "Greeting for morning"),
                        createTranslation(
                                "GREETING_AFTERNOON",
                                "PERSONA",
                                "HOME",
                                "GREETING",
                                "DISPLAY",
                                MultilingualContent.builder()
                                        .en("Good Afternoon")
                                        .km("ទិវាសួស្តី")
                                        .zh("下午好")
                                        .build(),
                                "Greeting for afternoon"),
                        createTranslation(
                                "GREETING_EVENING",
                                "PERSONA",
                                "HOME",
                                "GREETING",
                                "DISPLAY",
                                MultilingualContent.builder()
                                        .en("Good Evening")
                                        .km("សាយ័ណសួស្តី")
                                        .zh("晚上好")
                                        .build(),
                                "Greeting for evening"),
                        createTranslation(
                                "GREETING_NIGHT",
                                "PERSONA",
                                "HOME",
                                "GREETING",
                                "DISPLAY",
                                MultilingualContent.builder()
                                        .en("Good Night")
                                        .km("រាត្រីសួស្តី")
                                        .zh("晚安")
                                        .build(),
                                "Greeting for night"),
                        createTranslation(
                                "GREETING_HELLO",
                                "PERSONA",
                                "HOME",
                                "GREETING",
                                "DISPLAY",
                                MultilingualContent.builder()
                                        .en("Hello")
                                        .km("សួស្តី")
                                        .zh("你好")
                                        .build(),
                                "Greeting fallback"),
                        createTranslation(
                                "GREETING_FULL_FORMAT",
                                "PERSONA",
                                "HOME",
                                "GREETING",
                                "DISPLAY",
                                MultilingualContent.builder()
                                        .en("Hey Hey, %s %s")
                                        .km("ជំរាបសួរ, %s %s")
                                        .zh("嘿，%s %s")
                                        .build(),
                                "Full greeting format: first %s=greeting, second %s=user name"));

                translations.forEach(translation -> {
                    try {
                        translationRepository.save(translation);
                        log.info("Seeded translation: {}", translation.getCode());
                    } catch (Exception e) {
                        log.error("Error seeding translation", e);
                    }
                });
            }

            // Seed Product Categories
            if (productCategoryRepository.count() == 0) {
                ProductCategory transferCategory = createProductCategory(
                        "TRANSFER",
                        1L,
                        MultilingualContent.builder()
                                .en("Money Transfer")
                                .km("ផ្ទេរប្រាក់")
                                .zh("转账")
                                .build(),
                        MultilingualContent.builder()
                                .en("Transfer money to other accounts")
                                .km("ផ្ទេរប្រាក់ទៅគណនីផ្សេងទៀត")
                                .zh("转账到其他账户")
                                .build(),
                        "transfer_icon.png");

                ProductCategory billCategory = createProductCategory(
                        "BILL_PAYMENT",
                        2L,
                        MultilingualContent.builder()
                                .en("Bill Payments")
                                .km("ការបង់វិក្កយបត្រ")
                                .zh("账单支付")
                                .build(),
                        MultilingualContent.builder()
                                .en("Pay your bills")
                                .km("បង់វិក្កយបត្ររបស់អ្នក")
                                .zh("支付账单")
                                .build(),
                        "bill_icon.png");

                List<ProductCategory> categories = Arrays.asList(transferCategory, billCategory);
                categories.forEach(category -> {
                    try {
                        productCategoryRepository.save(category);
                        log.info("Seeded product category: {}", category.getCode());
                    } catch (Exception e) {
                        log.error("Error seeding product category", e);
                    }
                });

                // Seed Product Subcategories
                if (productSubcategoryRepository.count() == 0) {
                    ProductSubcategory localTransfer = createProductSubcategory(
                            transferCategory,
                            "LOCAL_TRANSFER",
                            MultilingualContent.builder()
                                    .en("Local Transfer")
                                    .km("ផ្ទេរប្រាក់ក្នុងស្រុក")
                                    .zh("本地转账")
                                    .build(),
                            MultilingualContent.builder()
                                    .en("Transfer within the country")
                                    .km("ផ្ទេរប្រាក់នៅក្នុងប្រទេស")
                                    .zh("国内转账")
                                    .build(),
                            "local_transfer_icon.png",
                            "TRANSFER");

                    ProductSubcategory utilityBills = createProductSubcategory(
                            billCategory,
                            "UTILITY_BILLS",
                            MultilingualContent.builder()
                                    .en("Utility Bills")
                                    .km("វិក្កយបត្រសេវាសាធារណៈ")
                                    .zh("公用事业账单")
                                    .build(),
                            MultilingualContent.builder()
                                    .en("Pay utility bills")
                                    .km("បង់វិក្កយបត្រសេវាសាធារណៈ")
                                    .zh("支付公用事业账单")
                                    .build(),
                            "utility_bills_icon.png",
                            "BILL_PAYMENT");

                    List<ProductSubcategory> subcategories = Arrays.asList(localTransfer, utilityBills);
                    subcategories.forEach(subcategory -> {
                        try {
                            productSubcategoryRepository.save(subcategory);
                            log.info("Seeded product subcategory: {}", subcategory.getCode());
                        } catch (Exception e) {
                            log.error("Error seeding product subcategory", e);
                        }
                    });

                    // Seed Products
                    if (productRepository.count() == 0) {
                        Product bankTransfer = createProduct(
                                localTransfer,
                                MultilingualContent.builder()
                                        .en("Bank Transfer")
                                        .km("ផ្ទេរប្រាក់ធនាគារ")
                                        .zh("银行转账")
                                        .build(),
                                MultilingualContent.builder()
                                        .en("Transfer to other bank accounts")
                                        .km("ផ្ទេរប្រាក់ទៅគណនីធនាគារផ្សេងទៀត")
                                        .zh("转账到其他银行账户")
                                        .build(),
                                "BANK_TRANSFER",
                                "bank_transfer_icon.png",
                                1);

                        Product electricityBill = createProduct(
                                utilityBills,
                                MultilingualContent.builder()
                                        .en("Electricity Bill")
                                        .km("វិក្កយបត្រអគ្គិសនី")
                                        .zh("电费")
                                        .build(),
                                MultilingualContent.builder()
                                        .en("Pay your electricity bills")
                                        .km("បង់វិក្កយបត្រអគ្គិសនី")
                                        .zh("支付电费")
                                        .build(),
                                "ELECTRICITY_BILL",
                                "electricity_bill_icon.png",
                                1);

                        List<Product> products = Arrays.asList(bankTransfer, electricityBill);
                        products.forEach(product -> {
                            try {
                                productRepository.save(product);
                                log.info("Seeded product: {}", product.getProductCode());
                            } catch (Exception e) {
                                log.error("Error seeding product", e);
                            }
                        });
                    }
                }
            }

            // Seed enumeration data
            // if (enumerationRepository.count() == 0) {
            // List<Enumeration> enumerations = Arrays.asList(
            // createEnumeration("TRANSACTION", "STATUS", "PENDING",
            // MultilingualContent.builder().en("Pending").km("កំពុងដំណើរការ").zh("待处理").build(),
            // MultilingualContent.builder().en("Transaction is being processed")
            // .km("ប្រតិបត្តិការកំពុងដំណើរការ").zh("交易正在处理中").build(),
            // 1, true),
            // createEnumeration("TRANSACTION", "STATUS", "COMPLETED",
            // MultilingualContent.builder().en("Completed").km("បានបញ្ចប់").zh("已完成").build(),
            // MultilingualContent.builder().en("Transaction has been completed")
            // .km("ប្រតិបត្តិការត្រូវបានបញ្ចប់").zh("交易已完成").build(),
            // 2, false),
            // createEnumeration("TRANSACTION", "STATUS", "FAILED",
            // MultilingualContent.builder().en("Failed").km("បរាជ័យ").zh("失败").build(),
            // MultilingualContent.builder().en("Transaction has
            // failed").km("ប្រតិបត្តិការបានបរាជ័យ")
            // .zh("交易失败").build(),
            // 3, false));
            //
            // enumerations.forEach(enumeration -> {
            // try {
            // enumerationRepository.save(enumeration);
            // log.info("Seeded enumeration: {}", enumeration.getCode());
            // } catch (Exception e) {
            // log.error("Error seeding enumeration", e);
            // }
            // });
            // }

            // Seed announcement data
            // if (announcementRepository.count() == 0) {
            // List<Announcement> announcements = Arrays.asList(
            // createAnnouncement("promo_icon.png",
            // MultilingualContent.builder().en("Special Year-End Promotion")
            // .km("ការផ្សព្វផ្សាយពិសេសចុងឆ្នាំ").zh("年终特惠").build(),
            // "Year-end special promotion with great rewards",
            // "Get up to 50% cashback on all transactions",
            // "wingbank://promotions/yearend",
            // "https://wingbank.com/promo/yearend", "DEEPLINK",
            // MultilingualContent.builder().en("Learn
            // More").km("ស្វែងយល់បន្ថែម").zh("了解更多").build(),
            // LocalDateTime.now(), LocalDateTime.now().plusMonths(1), "VERSION_A",
            // "ALL_USERS",
            // "HOME_TOP", "BANNER",
            // "{\"backgroundColor\":\"#FF0000\",\"textColor\":\"#FFFFFF\"}", 1,
            // 30, 5),
            // createAnnouncement("update_icon.png",
            // MultilingualContent.builder().en("App Update Available")
            // .km("មានការធ្វើបច្ចុប្បន្នភាពកម្មវិធី").zh("应用更新可用").build(),
            // "New version available with exciting features",
            // "Update now to experience the latest features", "wingbank://settings/update",
            // "https://wingbank.com/update", "POPUP",
            // MultilingualContent.builder().en("Update
            // Now").km("ធ្វើបច្ចុប្បន្នភាពឥឡូវនេះ")
            // .zh("立即更新").build(),
            // LocalDateTime.now(), LocalDateTime.now().plusWeeks(2), "VERSION_A",
            // "ALL_USERS",
            // "HOME_MIDDLE", "POPUP",
            // "{\"backgroundColor\":\"#0000FF\",\"textColor\":\"#FFFFFF\"}",
            // 2, 15, 3));
            //
            // announcements.forEach(announcement -> {
            // try {
            // announcementRepository.save(announcement);
            // log.info("Seeded announcement: {}", announcement.getName().getEn());
            // } catch (Exception e) {
            // log.error("Error seeding announcement", e);
            // }
            // });
            // }

            // Seed promotional data
            // if (promotionalRepository.count() == 0) {
            // List<Promotional> promotionals = Arrays.asList(
            // // 1. Summer Special Offer - For all users
            // createPromotional(
            // MultilingualContent.builder().en("Summer Special
            // Offer").km("កម្មវិធីពិសេសរដូវក្តៅ")
            // .zh("夏季特惠").build(),
            // MultilingualContent.builder().en("Get amazing rewards this summer")
            // .km("ទទួលបានរង្វាន់អស្ចារ្យនៅរដូវក្តៅនេះ").zh("这个夏天获得惊人奖励").build(),
            // "summer_promo.jpg", "summer_thumb.jpg", "summer_video.mp4",
            // "summer_secondary.jpg",
            // "wingbank://promotions/summer", "DEEPLINK",
            // "https://wingbank.com/promo/summer",
            // MultilingualContent.builder().en("Claim
            // Now").km("ទទួលយកឥឡូវនេះ").zh("立即领取").build(),
            // "HOME_TOP", "BANNER",
            // "{\"backgroundColor\":\"#FFD700\",\"textColor\":\"#000000\"}", 1,
            // 20, 3, LocalDateTime.now(), LocalDateTime.now().plusMonths(3), "SUMMER_2024",
            // "[]",
            // "{\"all\":true}", "ALL"),
            //
            // // 2. Premium Customer Exclusive - For premium segment
            // createPromotional(
            // MultilingualContent.builder().en("Premium Exclusive Rewards")
            // .km("រង្វាន់ផ្តាច់មុខសម្រាប់អតិថិជនពិសេស").zh("尊享会员专属奖励").build(),
            // MultilingualContent.builder().en("Special benefits for our premium
            // customers")
            // .km("អត្ថប្រយោជន៍ពិសេសសម្រាប់អតិថិជនកិត្តិយស").zh("尊享会员专属优惠").build(),
            // "premium_rewards.jpg", "premium_thumb.jpg", null, "premium_secondary.jpg",
            // "wingbank://premium/rewards", "DEEPLINK",
            // "https://wingbank.com/premium/rewards",
            // MultilingualContent.builder().en("View
            // Benefits").km("មើលអត្ថប្រយោជន៍").zh("查看权益")
            // .build(),
            // "HOME_TOP", "CARD",
            // "{\"backgroundColor\":\"#1E3A8A\",\"textColor\":\"#FFFFFF\"}", 2,
            // 30, 5, LocalDateTime.now(), LocalDateTime.now().plusMonths(6),
            // "PREMIUM_2024",
            // "[\"premium\"]", "{\"customerSegment\":\"premium\",\"minBalance\":10000}",
            // "ALL"),
            //
            // // 3. New User Welcome Offer - For new users
            // createPromotional(
            // MultilingualContent.builder().en("Welcome
            // Bonus").km("រង្វាន់ស្វាគមន៍").zh("迎新奖励")
            // .build(),
            // MultilingualContent.builder().en("Special offers for new users")
            // .km("ការផ្តល់ជូនពិសេសសម្រាប់អ្នកប្រើប្រាស់ថ្មី").zh("新用户专享优惠").build(),
            // "welcome_bonus.jpg", "welcome_thumb.jpg", "welcome_video.mp4", null,
            // "wingbank://welcome/bonus", "POPUP", "https://wingbank.com/welcome",
            // MultilingualContent.builder().en("Get
            // Started").km("ចាប់ផ្តើម").zh("立即开始").build(),
            // "HOME_MIDDLE", "POPUP",
            // "{\"backgroundColor\":\"#10B981\",\"textColor\":\"#FFFFFF\"}",
            // 3, 15, 2, LocalDateTime.now(), LocalDateTime.now().plusMonths(1),
            // "WELCOME_2024",
            // "[\"new_user\"]",
            // "{\"accountAge\":\"less_than_30_days\",\"firstLogin\":true}", "ALL"),
            //
            // // 4. Youth Banking Program - For young users
            // createPromotional(
            // MultilingualContent.builder().en("Youth Banking Program")
            // .km("កម្មវិធីធនាគារសម្រាប់យុវជន").zh("青年银行计划").build(),
            // MultilingualContent.builder().en("Special features for users aged 18-25")
            // .km("មុខងារពិសេសសម្រាប់អ្នកប្រើប្រាស់អាយុ ១៨-២៥ឆ្នាំ").zh("18-25岁用户专属功能")
            // .build(),
            // "youth_banking.jpg", "youth_thumb.jpg", null, "youth_secondary.jpg",
            // "wingbank://youth/program", "DEEPLINK", "https://wingbank.com/youth",
            // MultilingualContent.builder().en("Join
            // Now").km("ចូលរួមឥឡូវនេះ").zh("立即加入").build(),
            // "HOME_MIDDLE", "BANNER",
            // "{\"backgroundColor\":\"#7C3AED\",\"textColor\":\"#FFFFFF\"}",
            // 4, 25, 4, LocalDateTime.now(), LocalDateTime.now().plusMonths(12),
            // "YOUTH_2024",
            // "[\"youth\"]", "{\"ageRange\":{\"min\":18,\"max\":25}}", "ALL"),
            //
            // // 5. Business Account Promotion - For business users
            // createPromotional(
            // MultilingualContent.builder().en("Business Account Benefits")
            // .km("អត្ថប្រយោជន៍គណនីអាជីវកម្ម").zh("商业账户权益").build(),
            // MultilingualContent.builder().en("Exclusive offers for business account
            // holders")
            // .km("ការផ្តល់ជូនពិសេសសម្រាប់ម្ចាស់គណនីអាជីវកម្ម").zh("商业账户专属优惠").build(),
            // "business_promo.jpg", "business_thumb.jpg", "business_video.mp4",
            // "business_secondary.jpg", "wingbank://business/benefits", "DEEPLINK",
            // "https://wingbank.com/business/benefits",
            // MultilingualContent.builder().en("Learn
            // More").km("ស្វែងយល់បន្ថែម").zh("了解更多").build(),
            // "HOME_BOTTOM", "CARD",
            // "{\"backgroundColor\":\"#374151\",\"textColor\":\"#FFFFFF\"}", 5,
            // 30, 3, LocalDateTime.now(), LocalDateTime.now().plusMonths(6),
            // "BUSINESS_2024",
            // "[\"business\"]", "{\"accountType\":\"business\",\"minTransactions\":5}",
            // "ALL"));
            //
            // promotionals.forEach(promotional -> {
            // try {
            // promotionalRepository.save(promotional);
            // log.info("Seeded promotional: {}", promotional.getTitle().getEn());
            // } catch (Exception e) {
            // log.error("Error seeding promotional", e);
            // }
            // });
            // }
            log.info("Data seeding completed!");
        };
    }

    private Favorite createFavorite(
            String serviceType, String serviceCode, String displayName, String description, String serviceIcon) {
        Favorite favorite = new Favorite();
        favorite.setServiceType(serviceType);
        favorite.setServiceCode(serviceCode);
        favorite.setDisplayName(displayName);
        favorite.setDescription(description);
        favorite.setServiceIcon(serviceIcon);
        favorite.setAppVersion("1.0.0");
        favorite.setStatus(StatusType.ACTIVE);
        return favorite;
    }

    private Schedule createSchedule(
            String serviceType,
            String serviceCode,
            String scheduleType,
            String displayName,
            String description,
            String serviceIcon,
            Map<String, Object> requiredFields,
            BigDecimal maxAmount,
            BigDecimal minAmount,
            Integer maxScheduleCount) {
        ObjectMapperUtils objectMapperUtils = new ObjectMapperUtils();
        Schedule schedule = new Schedule();
        schedule.setServiceType(serviceType);
        schedule.setServiceCode(serviceCode);
        schedule.setScheduleType(scheduleType);
        schedule.setDisplayName(displayName);
        schedule.setDescription(description);
        schedule.setServiceIcon(serviceIcon);
        schedule.setRequiredFields(requiredFields);
        schedule.setMaxAmount(maxAmount);
        schedule.setMinAmount(minAmount);
        schedule.setStatus(StatusType.ACTIVE);
        schedule.setMaxScheduleCount(maxScheduleCount);
        return schedule;
    }

    private Translation createTranslation(
            String code,
            String appCode,
            String category,
            String subCategory,
            String type,
            MultilingualContent name,
            String description) {
        return Translation.builder()
                .code(code)
                .appCode(appCode)
                .category(category)
                .subCategory(subCategory)
                .type(type)
                .name(name)
                .description(description)
                .status(StatusType.ACTIVE)
                .build();
    }

    private ProductCategory createProductCategory(
            String code,
            Long productCategoryId,
            MultilingualContent name,
            MultilingualContent description,
            String iconUrl) {
        ProductCategory category = new ProductCategory();
        category.setCode(code);
        category.setProductCategoryId(productCategoryId);
        category.setName(name);
        category.setDescription(description);
        category.setIconUrl(iconUrl);
        category.setSort(1);
        category.setMaintenanceMode(0);
        category.setDisplayOrder(1);
        category.setStatus(StatusType.ACTIVE);
        return category;
    }

    private ProductSubcategory createProductSubcategory(
            ProductCategory category,
            String code,
            MultilingualContent name,
            MultilingualContent description,
            String primaryIconUrl,
            String serviceType) {
        ProductSubcategory subcategory = new ProductSubcategory();
        subcategory.setCategory(category);
        subcategory.setCode(code);
        subcategory.setName(name);
        subcategory.setDescription(description);
        subcategory.setPrimaryIconUrl(primaryIconUrl);
        subcategory.setServiceType(serviceType);
        subcategory.setSort(1);
        subcategory.setIsActive(true);
        subcategory.setMaintenanceMode(false);
        subcategory.setStatus(StatusType.ACTIVE);

        return subcategory;
    }

    private Product createProduct(
            ProductSubcategory subcategory,
            MultilingualContent name,
            MultilingualContent description,
            String productCode,
            String primaryIconUrl,
            Integer displayOrder) {
        Product product = new Product();
        product.setSubcategory(subcategory);
        product.setName(name);
        product.setDescription(description);
        product.setProductCode(productCode);
        product.setPrimaryIconUrl(primaryIconUrl);
        product.setDisplayOrder(displayOrder);
        product.setMaintenanceMode(false);
        product.setStatus(StatusType.ACTIVE);
        return product;
    }

    private Enumeration createEnumeration(
            String category,
            String subCategory,
            String code,
            MultilingualContent value,
            MultilingualContent displayValue,
            Integer displayOrder,
            Boolean isDefault) {
        return Enumeration.builder()
                .category(category)
                .subCategory(subCategory)
                .code(code)
                .value(value)
                .displayValue(displayValue)
                .displayOrder(displayOrder)
                .isDefault(isDefault)
                .build();
    }

    private Announcement createAnnouncement(
            String iconUrl,
            MultilingualContent name,
            String longText,
            String description,
            String deeplinkUrl,
            String buttonUrl,
            String actionType,
            MultilingualContent actionButtonText,
            LocalDateTime effectiveFrom,
            LocalDateTime effectiveTo,
            String variant,
            String segment,
            String displayLocation,
            String displayType,
            String displayStyle,
            Integer displayOrder,
            Integer displayDuration,
            Integer maxImpressions) {

        return Announcement.builder()
                .iconUrl(iconUrl)
                .name(name)
                .longText(longText)
                .discription(description)
                .deeplinkUrl(deeplinkUrl)
                .buttonUrl(buttonUrl)
                .actionType(actionType)
                .actionButtonText(actionButtonText)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .variant(variant)
                .segment(segment)
                .displayLocation(displayLocation)
                .displayType(displayType)
                .displayStyle(displayStyle)
                .displayOrder(displayOrder)
                .displayDuration(displayDuration)
                .maxImpressions(maxImpressions)
                .enableTracking(true)
                .targetPlatforms("ALL")
                .targetSegments("[]")
                .targetAudience("{}")
                .analyticsConfig("{\"enabled\":true}")
                .build();
    }

    // Updated helper method with targeting parameters
    private Promotional createPromotional(
            MultilingualContent title,
            MultilingualContent description,
            String imageUrl,
            String thumbnailUrl,
            String videoUrl,
            String imageUrlSecondary,
            String deeplinkUrl,
            String actionType,
            String actionUrl,
            MultilingualContent actionButtonText,
            String displayLocation,
            String displayType,
            String displayStyle,
            Integer displayOrder,
            Integer displayDuration,
            Integer maxImpressions,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String campaignId,
            String targetSegments,
            String targetAudience,
            String targetPlatforms) {

        return Promotional.builder()
                .title(title)
                .description(description)
                .imageUrl(imageUrl)
                .thumbnailUrl(thumbnailUrl)
                .videoUrl(videoUrl)
                .imageUrlSecondary(imageUrlSecondary)
                .deeplinkUrl(deeplinkUrl)
                .actionType(actionType)
                .actionUrl(actionUrl)
                .actionButtonText(actionButtonText)
                .displayLocation(displayLocation)
                .displayType(displayType)
                .displayStyle(displayStyle)
                .displayOrder(displayOrder)
                .displayDuration(displayDuration)
                .maxImpressions(maxImpressions)
                .effectiveFrom(startDate)
                .effectiveTo(endDate)
                .targetSegments(targetSegments)
                .targetAudience(targetAudience)
                .targetPlatforms(targetPlatforms)
                .isDynamic(false)
                .personalizationRules("{}")
                .fallbackContent("{}")
                .campaignId(campaignId)
                .promotionCode(campaignId.toLowerCase())
                .marketingTags("promotion,campaign," + campaignId.toLowerCase())
                .enableTracking(true)
                .analyticsConfig("{\"enabled\":true,\"trackImpressions\":true,\"trackClicks\":true}")
                .build();
    }
}
