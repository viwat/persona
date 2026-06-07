package com.example.persona.seeder;

import com.example.persona.enums.StatusType;
import com.example.persona.model.MultilingualContent;
import com.example.persona.theme.model.AccentColor;
import com.example.persona.theme.model.Theme;
import com.example.persona.theme.model.ThemeCategory;
import com.example.persona.theme.model.ThemeIconSet;
import com.example.persona.theme.model.ThemePackage;
import com.example.persona.theme.repository.AccentColorRepository;
import com.example.persona.theme.repository.ThemeCategoryRepository;
import com.example.persona.theme.repository.ThemeIconSetRepository;
import com.example.persona.theme.repository.ThemePackageRepository;
import com.example.persona.theme.repository.ThemeRepository;
import com.example.persona.widget.model.Widget;
import com.example.persona.widget.repository.WidgetRepository;
import java.time.LocalDate;
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
public class ThemeSeeder {

    private final AccentColorRepository accentColorRepository;
    private final ThemeCategoryRepository themeCategoryRepository;
    private final ThemeIconSetRepository themeIconSetRepository;
    private final ThemePackageRepository themePackageRepository;
    private final ThemeRepository themeRepository;
    private final WidgetRepository widgetRepository;

    @Bean
    @Profile("seed")
    public CommandLineRunner seedThemeData() {
        return args -> {
            log.info("Starting theme data seeding...");

            // Seed Accent Colors
            log.info("Seeding accent colors...");
            if (accentColorRepository.count() == 0) {
                List<AccentColor> accentColors = Arrays.asList(
                        createAccentColor(
                                MultilingualContent.builder()
                                        .en("Royal Blue")
                                        .km("ខៀវក្រមួន")
                                        .zh("宝蓝色")
                                        .build(),
                                "#4169E1",
                                "rgb(65, 105, 225)",
                                "royal_blue_icon.png"),
                        createAccentColor(
                                MultilingualContent.builder()
                                        .en("Emerald Green")
                                        .km("បៃតងមរកត")
                                        .zh("翠绿色")
                                        .build(),
                                "#50C878",
                                "rgb(80, 200, 120)",
                                "emerald_green_icon.png"),
                        createAccentColor(
                                MultilingualContent.builder()
                                        .en("Ruby Red")
                                        .km("ក្រហមទទឹម")
                                        .zh("红宝石色")
                                        .build(),
                                "#E0115F",
                                "rgb(224, 17, 95)",
                                "ruby_red_icon.png"),
                        createAccentColor(
                                MultilingualContent.builder()
                                        .en("Amethyst Purple")
                                        .km("ស្វាយអាមេទីស")
                                        .zh("紫水晶色")
                                        .build(),
                                "#9966CC",
                                "rgb(153, 102, 204)",
                                "amethyst_purple_icon.png"));

                accentColors.forEach(color -> {
                    try {
                        accentColorRepository.save(color);
                        log.info(
                                "Seeded accent color: {} - {} ({})",
                                color.getColorCode(),
                                color.getDisplayName().getEn(),
                                color.getRgbCode());
                    } catch (Exception e) {
                        log.error("Error seeding accent color: {}", color.getColorCode(), e);
                    }
                });
                log.info("Completed seeding {} accent colors", accentColors.size());
            } else {
                log.info("Accent colors already exist, skipping...");
            }

            // Seed Theme Categories
            log.info("Seeding theme categories...");
            if (themeCategoryRepository.count() == 0) {
                List<ThemeCategory> themeCategories = Arrays.asList(
                        createThemeCategory(
                                "SEASONAL",
                                "WING",
                                "SEASONAL_2024",
                                MultilingualContent.builder()
                                        .en("Seasonal Themes")
                                        .km("រូបរាងតាមរដូវកាល")
                                        .zh("季节性主题")
                                        .build(),
                                MultilingualContent.builder()
                                        .en("Special themes for different seasons and occasions")
                                        .km("រូបរាងពិសេសសម្រាប់រដូវកាលនិងឱកាសផ្សេងៗ")
                                        .zh("适用于不同季节和场合的特殊主题")
                                        .build(),
                                1,
                                LocalDate.now(),
                                LocalDate.now().plusYears(1),
                                "ALL",
                                "ALL",
                                "DEFAULT",
                                "STANDARD",
                                "1.0",
                                "seasonal_badge.png",
                                "seasonal_display.png"),
                        createThemeCategory(
                                "PREMIUM",
                                "WING",
                                "PREMIUM_2024",
                                MultilingualContent.builder()
                                        .en("Premium Themes")
                                        .km("រូបរាងប្រណីត")
                                        .zh("高级主题")
                                        .build(),
                                MultilingualContent.builder()
                                        .en("Exclusive premium themes for distinguished users")
                                        .km("រូបរាងប្រណីតផ្តាច់មុខសម្រាប់អ្នកប្រើប្រាស់ពិសេស")
                                        .zh("为尊贵用户提供的专属高级主题")
                                        .build(),
                                2,
                                LocalDate.now(),
                                LocalDate.now().plusYears(1),
                                "PREMIUM",
                                "ALL",
                                "DEFAULT",
                                "PREMIUM",
                                "1.0",
                                "premium_badge.png",
                                "premium_display.png"));

                themeCategories.forEach(category -> {
                    try {
                        themeCategoryRepository.save(category);
                        log.info(
                                "Seeded theme category: {} - {} ({})",
                                category.getCode(),
                                category.getName().getEn(),
                                category.getThemeType());
                    } catch (Exception e) {
                        log.error("Error seeding theme category: {}", category.getCode(), e);
                    }
                });
                log.info("Completed seeding {} theme categories", themeCategories.size());
            } else {
                log.info("Theme categories already exist, skipping...");
            }

            // Seed Theme Icon Sets
            log.info("Seeding theme icon sets...");
            if (themeIconSetRepository.count() == 0) {
                List<ThemeIconSet> themeIconSets = Arrays.asList(
                        createThemeIconSet(
                                "HOME_ICON",
                                "icons/home.png",
                                "PNG",
                                "NAVIGATION",
                                "{\"size\":\"24x24\",\"style\":\"filled\"}"),
                        createThemeIconSet(
                                "TRANSFER_ICON",
                                "icons/transfer.png",
                                "PNG",
                                "TRANSACTION",
                                "{\"size\":\"24x24\",\"style\":\"outlined\"}"),
                        createThemeIconSet(
                                "SETTINGS_ICON",
                                "icons/settings.png",
                                "PNG",
                                "SYSTEM",
                                "{\"size\":\"24x24\",\"style\":\"filled\"}"),
                        createThemeIconSet(
                                "NOTIFICATION_ICON",
                                "icons/notification.png",
                                "PNG",
                                "SYSTEM",
                                "{\"size\":\"24x24\",\"style\":\"outlined\"}"));

                themeIconSets.forEach(iconSet -> {
                    try {
                        themeIconSetRepository.save(iconSet);
                        log.info(
                                "Seeded theme icon set: {} - {} ({})",
                                iconSet.getCode(),
                                iconSet.getIconCategory(),
                                iconSet.getIconType());
                    } catch (Exception e) {
                        log.error("Error seeding theme icon set: {}", iconSet.getCode(), e);
                    }
                });
                log.info("Completed seeding {} theme icon sets", themeIconSets.size());
            } else {
                log.info("Theme icon sets already exist, skipping...");
            }

            // Seed Theme Packages
            log.info("Seeding theme packages...");
            if (themePackageRepository.count() == 0) {
                List<ThemePackage> themePackages = Arrays.asList(
                        createThemePackage(
                                "GRADIENT",
                                "themes/seasonal_gradient.png",
                                "#FFFFFF",
                                "Roboto",
                                "#F5F5F5",
                                "themes/seasonal_secondary_bg.png",
                                "SOLID",
                                "mascots/seasonal_mascot.png",
                                "mascots/seasonal_mascot_secondary.png",
                                "BOTTOM_RIGHT",
                                "MEDIUM",
                                "#4169E1",
                                "ANIMATED",
                                "BOUNCE",
                                "NORMAL",
                                "LEFT_RIGHT",
                                "INFINITE"),
                        createThemePackage(
                                "SOLID",
                                "themes/premium_background.png",
                                "#000000",
                                "Helvetica",
                                "#2C2C2C",
                                "themes/premium_secondary_bg.png",
                                "PATTERN",
                                "mascots/premium_mascot.png",
                                "mascots/premium_mascot_secondary.png",
                                "BOTTOM_LEFT",
                                "LARGE",
                                "#FFD700",
                                "STATIC",
                                null,
                                null,
                                null,
                                null));

                themePackages.forEach(pkg -> {
                    try {
                        themePackageRepository.save(pkg);
                        log.info(
                                "Seeded theme package: {} - {} ({})",
                                pkg.getBackgroundType(),
                                pkg.getTextFontStyle(),
                                pkg.getMascotStyle());
                    } catch (Exception e) {
                        log.error("Error seeding theme package: {}", pkg.getBackgroundType(), e);
                    }
                });
                log.info("Completed seeding {} theme packages", themePackages.size());
            } else {
                log.info("Theme packages already exist, skipping...");
            }

            // Seed Widgets
            log.info("Seeding widgets...");
            // if (widgetRepository.count() == 0) {
            // List<Widget> widgets = Arrays.asList(
            // createWidget("QUICK_ACTION", "TRANSFER", "RETAIL", "MASS",
            // createMultilingualContent("Quick Transfer", "ការផ្ទេរប្រាក់រហ័ស", "快速转账"),
            // createMultilingualContent("Transfer money quickly", "ផ្ទេរប្រាក់យ៉ាងរហ័ស",
            // "快速转账资金"),
            // "transfer_icon.png", true, "1", "transfer_preview.png",
            // "wingbank://transfer", null,
            // "1.0.0", "{\"maxAmount\": 1000}", "{\"category\": \"financial\"}", "1.0.0",
            // "amount",
            // "number", "currency", "string", "recipient", "string", null, null, null,
            // null),
            // createWidget("DASHBOARD", "INVESTMENT", "PRIORITY", "BUSINESS",
            // createMultilingualContent("Investment Portfolio", "ការវិនិយោគ", "投资组合"),
            // createMultilingualContent("Manage your investments",
            // "គ្រប់គ្រងការវិនិយោគរបស់អ្នក",
            // "管理您的投资"),
            // "investment_icon.png", true, "2", "investment_preview.png",
            // "wingbank://investment",
            // "chart_preview.png", "2.0.0", "{\"viewType\": \"chart\"}", "{\"category\":
            // \"wealth\"}",
            // "2.0.0", "period", "string", "type", "string", null, null, null, null, null,
            // null),
            // createWidget("QUICK_ACTION", "PAYMENT", "MASS", "YOUTH",
            // createMultilingualContent("Student Payment", "ការបង់ប្រាក់សិស្ស", "学生支付"),
            // createMultilingualContent("Pay your tuition fees", "បង់ថ្លៃសិក្សា", "支付学费"),
            // "payment_icon.png", true, "3", "payment_preview.png", "wingbank://payment",
            // null,
            // "1.0.0", "{\"category\": \"education\"}", "{\"type\": \"student\"}", "1.0.0",
            // "amount",
            // "number", "institution", "string", "semester", "string", null, null, null,
            // null));
            //
            // widgets.forEach(widget -> {
            // try {
            // widgetRepository.save(widget);
            // log.info("Seeded widget: {} - {} ({})", widget.getCode(),
            // widget.getName().getEn(),
            // widget.getType());
            // } catch (Exception e) {
            // log.error("Error seeding widget: {}", widget.getCode(), e);
            // }
            // });
            // log.info("Completed seeding {} widgets", widgets.size());
            // } else {
            // log.info("Widgets already exist, skipping...");
            // }

            log.info("Theme data seeding completed successfully!");
        };
    }

    private AccentColor createAccentColor(
            MultilingualContent displayName, String colorCode, String rgbCode, String iconUrl) {
        AccentColor accentColor = new AccentColor();
        accentColor.setDisplayName(displayName);
        accentColor.setColorCode(colorCode);
        accentColor.setRgbCode(rgbCode);
        accentColor.setIconUrl(iconUrl);
        accentColor.setStatus(StatusType.ACTIVE);
        return accentColor;
    }

    private ThemeCategory createThemeCategory(
            String category,
            String appId,
            String code,
            MultilingualContent name,
            MultilingualContent description,
            int displayOrder,
            LocalDate effectiveDate,
            LocalDate expirationDate,
            String customerSegment,
            String customerSubSegment,
            String variant,
            String themeType,
            String themeVersion,
            String badgeIcon,
            String displayImage) {

        ThemeCategory themeCategory = new ThemeCategory();
        themeCategory.setCategory(category);
        themeCategory.setAppId(appId);
        themeCategory.setCode(code);
        themeCategory.setName(name);
        themeCategory.setDescription(description);
        themeCategory.setDisplayOrder(displayOrder);
        themeCategory.setEffectiveDate(effectiveDate);
        themeCategory.setExpirationDate(expirationDate);
        themeCategory.setCustomerSegment(customerSegment);
        themeCategory.setCustomerSubSegment(customerSubSegment);
        themeCategory.setVariant(variant);
        themeCategory.setThemeType(themeType);
        themeCategory.setThemeVersion(themeVersion);
        themeCategory.setBadgeIcon(badgeIcon);
        themeCategory.setDisplayImage(displayImage);
        return themeCategory;
    }

    private ThemeIconSet createThemeIconSet(
            String code, String iconUrl, String iconType, String iconCategory, String metadata) {

        ThemeIconSet themeIconSet = new ThemeIconSet();
        themeIconSet.setCode(code);
        themeIconSet.setIconUrl(iconUrl);
        themeIconSet.setIconType(iconType);
        themeIconSet.setIconCategory(iconCategory);
        themeIconSet.setMetadata(metadata);
        return themeIconSet;
    }

    private ThemePackage createThemePackage(
            String backgroundType,
            String backgroundUrl,
            String textColor,
            String textFontStyle,
            String secondaryColor,
            String secondaryBackgroundUrl,
            String secondaryBackgroundType,
            String mascotImageUrl,
            String secondaryMascotImageUrl,
            String mascotLocation,
            String mascotSize,
            String mascotColor,
            String mascotStyle,
            String mascotAnimation,
            String mascotAnimationSpeed,
            String mascotAnimationDirection,
            String mascotAnimationRepeat) {

        ThemePackage themePackage = new ThemePackage();
        themePackage.setBackgroundType(backgroundType);
        themePackage.setBackgroundUrl(backgroundUrl);
        themePackage.setTextColor(textColor);
        themePackage.setTextFontStyle(textFontStyle);
        themePackage.setSecondaryColor(secondaryColor);
        themePackage.setSecondaryBackgroundUrl(secondaryBackgroundUrl);
        themePackage.setSecondaryBackgroundType(secondaryBackgroundType);
        themePackage.setMascotImageUrl(mascotImageUrl);
        themePackage.setSecondaryMascotImageUrl(secondaryMascotImageUrl);
        themePackage.setMascotLocation(mascotLocation);
        themePackage.setMascotSize(mascotSize);
        themePackage.setMascotColor(mascotColor);
        themePackage.setMascotStyle(mascotStyle);
        themePackage.setMascotAnimation(mascotAnimation);
        themePackage.setMascotAnimationSpeed(mascotAnimationSpeed);
        themePackage.setMascotAnimationDirection(mascotAnimationDirection);
        themePackage.setMascotAnimationRepeat(mascotAnimationRepeat);

        return themePackage;
    }

    private Theme createTheme(
            ThemeCategory category,
            String code,
            String layout,
            MultilingualContent displayName,
            MultilingualContent description,
            int displayOrder,
            LocalDate effectiveDate,
            LocalDate expirationDate,
            String customerSegment,
            String customerSubSegment,
            String variant,
            String themeType,
            String themeVersion,
            String appVersion,
            String badgeIconKm,
            String badgeIconZh,
            String previewImageUrl,
            String animated,
            String metadata,
            ThemePackage lightModeThemeId,
            ThemePackage darkModeThemeId) {

        Theme theme = new Theme();
        theme.setCategory(category);
        theme.setCode(code);
        theme.setLayout(layout);
        theme.setDisplayName(displayName);
        theme.setDescription(description);
        theme.setDisplayOrder(displayOrder);
        theme.setEffectiveDate(effectiveDate);
        theme.setExpirationDate(expirationDate);
        theme.setCustomerSegment(customerSegment);
        theme.setCustomerSubSegment(customerSubSegment);
        theme.setVariant(variant);
        theme.setThemeType(themeType);
        theme.setThemeVersion(themeVersion);
        theme.setAppVersion(appVersion);
        theme.setBadgeIconKm(badgeIconKm);
        theme.setBadgeIconZh(badgeIconZh);
        theme.setPreviewImageUrl(previewImageUrl);
        theme.setAnimated(animated);
        theme.setMetadata(metadata);
        theme.setLightModeThemeId(lightModeThemeId);
        theme.setDarkModeThemeId(darkModeThemeId);

        return theme;
    }

    private Widget createWidget(
            String type,
            String code,
            String customerSegment,
            String customerSubSegment,
            MultilingualContent name,
            MultilingualContent description,
            String iconUrl,
            Boolean isDefault,
            String displayOrder,
            String previewUrl,
            String deepLink,
            String secondaryPreviewUrl,
            String widgetVersion,
            String params,
            String metadata,
            String appVersion,
            String param1Key,
            String param1Type,
            String param2Key,
            String param2Type,
            String param3Key,
            String param3Type,
            String param4Key,
            String param4Type,
            String param5Key,
            String param5Type) {

        return Widget.builder()
                .type(type)
                .code(code)
                .customerSegment(customerSegment)
                .customerSubSegment(customerSubSegment)
                .name(name)
                .description(description)
                .iconUrl(iconUrl)
                .isDefault(isDefault)
                .displayOrder(displayOrder)
                .previewUrl(previewUrl)
                .deepLink(deepLink)
                .secondaryPreviewUrl(secondaryPreviewUrl)
                .widgetVersion(widgetVersion)
                .params(params)
                .metadata(metadata)
                .appVersion(appVersion)
                .param1Key(param1Key)
                .param1Type(param1Type)
                .param2Key(param2Key)
                .param2Type(param2Type)
                .param3Key(param3Key)
                .param3Type(param3Type)
                .param4Key(param4Key)
                .param4Type(param4Type)
                .param5Key(param5Key)
                .param5Type(param5Type)
                .build();
    }

    private MultilingualContent createMultilingualContent(String en, String km, String zh) {
        return MultilingualContent.builder().en(en).km(km).zh(zh).build();
    }
}
