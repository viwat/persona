package com.example.persona.home.service;

import com.example.persona.config.UserContextHolder;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.exception.BusinessException;
import com.example.persona.dto.UserContext;
import com.example.persona.home.dto.GreetingResponse;
import com.example.persona.home.dto.HomeScreenConfigResponse;
import com.example.persona.home.dto.UpdateBannerResponse;
import com.example.persona.profile.repository.CustomerProfileRepository;
import com.example.persona.theme.dto.response.ThemeDownloadResponse;
import com.example.persona.theme.model.UserPersona;
import com.example.persona.theme.repository.UserPersonaRepository;
import com.example.persona.theme.service.ThemeService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeScreenConfigService {

    private final ThemeService themeService;
    private final UserPersonaRepository userPersonaRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final GreetingService greetingService;
    private final AppUpdateService appUpdateService;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Transactional(readOnly = true)
    public HomeScreenConfigResponse getHomeScreenConfig(CustomerBaseRequest request) {
        log.debug("Getting home screen config for customer: {}", request.getCustomerNo());

        String customerNo = request.getCustomerNo();
        if (customerNo == null) {
            UserContext ctx = UserContextHolder.getCurrentContext();
            if (ctx != null && ctx.getUserId() != null) {
                customerNo = ctx.getUserId();
            }
        }

        if (customerNo == null) {
            throw new BusinessException("Customer number is required");
        }

        // Get user's active theme
        HomeScreenConfigResponse.ThemeConfig themeConfig = getThemeConfig(customerNo);

        // Get personalized greeting
        GreetingResponse greeting = greetingService.getGreeting(customerNo);

        // Get update banner status
        UpdateBannerResponse updateBanner = appUpdateService.checkUpdate(request);

        // Get service configuration
        List<HomeScreenConfigResponse.ServiceConfigResponse> services = getServiceConfig(customerNo);

        // Get navigation configuration
        HomeScreenConfigResponse.NavigationConfigResponse navigation = getNavigationConfig(themeConfig);

        // Get quick access configuration
        HomeScreenConfigResponse.QuickAccessConfigResponse quickAccess = getQuickAccessConfig();

        return HomeScreenConfigResponse.builder()
                .theme(themeConfig)
                .greeting(greeting)
                .updateBanner(updateBanner)
                .services(services)
                .navigation(navigation)
                .quickAccess(quickAccess)
                .build();
    }

    private HomeScreenConfigResponse.ThemeConfig getThemeConfig(String customerNo) {
        Optional<UserPersona> userPersonaOpt = userPersonaRepository.findByCustomerNo(customerNo);
        String themeCode = "default"; // Default theme code
        String accentColor = null;

        if (userPersonaOpt.isPresent()) {
            UserPersona userPersona = userPersonaOpt.get();
            if (userPersona.getThemeCode() != null) {
                themeCode = userPersona.getThemeCode();
            }
            accentColor = userPersona.getAccentColor();
        }

        try {
            ThemeDownloadResponse themeDownload = themeService.downloadTheme(themeCode);
            return HomeScreenConfigResponse.ThemeConfig.builder()
                    .themeCode(themeCode)
                    .lightMode(themeDownload.getLightMode())
                    .darkMode(themeDownload.getDarkMode())
                    .accentColor(accentColor)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to load theme: {}, using default", themeCode, e);
            return HomeScreenConfigResponse.ThemeConfig.builder()
                    .themeCode(themeCode)
                    .accentColor(accentColor)
                    .build();
        }
    }

    private List<HomeScreenConfigResponse.ServiceConfigResponse> getServiceConfig(String customerNo) {
        Optional<UserPersona> userPersonaOpt = userPersonaRepository.findByCustomerNo(customerNo);
        List<String> serviceOrder;

        if (userPersonaOpt.isPresent() && userPersonaOpt.get().getServiceOrders() != null) {
            serviceOrder = userPersonaOpt.get().getServiceOrders();
        } else {
            // Default service order
            serviceOrder = List.of("account", "topup", "paybills", "transfer", "saveforgoal", "codetowing");
        }

        // Build service config from theme icons or default
        List<HomeScreenConfigResponse.ServiceConfigResponse> services = new ArrayList<>();
        int order = 0;
        for (String serviceCode : serviceOrder) {
            services.add(HomeScreenConfigResponse.ServiceConfigResponse.builder()
                    .code(serviceCode)
                    .label(getServiceLabel(serviceCode))
                    .iconUrl(getServiceIconUrl(serviceCode))
                    .iconType("service")
                    .displayOrder(order++)
                    .enabled(true)
                    .build());
        }

        return services;
    }

    private String getServiceLabel(String code) {
        return switch (code.toLowerCase()) {
            case "account" -> "Account";
            case "topup" -> "Top-Up";
            case "paybills" -> "Pay Bills";
            case "transfer" -> "Transfer";
            case "saveforgoal" -> "Save For Goal";
            case "codetowing" -> "Code To Wing";
            default -> code;
        };
    }

    private String getServiceIconUrl(String code) {
        // In a real implementation, this would fetch from theme icons or asset
        // service
        return "/icons/services/" + code + ".png";
    }

    private HomeScreenConfigResponse.NavigationConfigResponse getNavigationConfig(
            HomeScreenConfigResponse.ThemeConfig themeConfig) {
        String activeColor = themeConfig.getAccentColor() != null ? themeConfig.getAccentColor() : "#50C878"; // Default
        // green

        List<HomeScreenConfigResponse.NavigationConfigResponse.NavigationItem> items = List.of(
                HomeScreenConfigResponse.NavigationConfigResponse.NavigationItem.builder()
                        .code("home")
                        .label("Home")
                        .iconUrl("/icons/navigation/home.png")
                        .isActive(true)
                        .order(0)
                        .build(),
                HomeScreenConfigResponse.NavigationConfigResponse.NavigationItem.builder()
                        .code("cards")
                        .label("Cards")
                        .iconUrl("/icons/navigation/cards.png")
                        .isActive(false)
                        .order(1)
                        .build(),
                HomeScreenConfigResponse.NavigationConfigResponse.NavigationItem.builder()
                        .code("help")
                        .label("Help")
                        .iconUrl("/icons/navigation/help.png")
                        .isActive(false)
                        .order(2)
                        .build(),
                HomeScreenConfigResponse.NavigationConfigResponse.NavigationItem.builder()
                        .code("profile")
                        .label("Profile")
                        .iconUrl("/icons/navigation/profile.png")
                        .isActive(false)
                        .order(3)
                        .build());

        return HomeScreenConfigResponse.NavigationConfigResponse.builder()
                .items(items)
                .activeColor(activeColor)
                .build();
    }

    private HomeScreenConfigResponse.QuickAccessConfigResponse getQuickAccessConfig() {
        List<HomeScreenConfigResponse.QuickAccessConfigResponse.QuickAccessItem> items = List.of(
                HomeScreenConfigResponse.QuickAccessConfigResponse.QuickAccessItem.builder()
                        .code("rewards")
                        .label("Rewards")
                        .iconUrl("/icons/quickaccess/rewards.png")
                        .order(0)
                        .enabled(true)
                        .build(),
                HomeScreenConfigResponse.QuickAccessConfigResponse.QuickAccessItem.builder()
                        .code("favorites")
                        .label("Favorites")
                        .iconUrl("/icons/quickaccess/favorites.png")
                        .order(1)
                        .enabled(true)
                        .build(),
                HomeScreenConfigResponse.QuickAccessConfigResponse.QuickAccessItem.builder()
                        .code("notifications")
                        .label("Notifications")
                        .iconUrl("/icons/quickaccess/notifications.png")
                        .order(2)
                        .enabled(true)
                        .build(),
                HomeScreenConfigResponse.QuickAccessConfigResponse.QuickAccessItem.builder()
                        .code("scan")
                        .label("Scan")
                        .iconUrl("/icons/quickaccess/scan.png")
                        .order(3)
                        .enabled(true)
                        .build());

        return HomeScreenConfigResponse.QuickAccessConfigResponse.builder()
                .items(items)
                .build();
    }
}
