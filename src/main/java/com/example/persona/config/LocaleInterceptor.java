package com.example.persona.config;

import com.example.persona.dto.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@AllArgsConstructor
public class LocaleInterceptor implements HandlerInterceptor {

    private static final String DEFAULT_LANG = "en";
    private static final String LANG_HEADER = "User-Lang";
    private static final String PLATFORM_HEADER = "User-Platform";
    private static final String APP_ID_HEADER = "User-App-Id";
    private static final String APP_PHONE_NUMBER_HEADER = "User-App-Phone-Number";
    private static final String REQUEST_ID_HEADER = "Request-Id";
    private static final String SESSION_ID_HEADER = "Session-Id";
    private static final String MDC_REQUEST_ID_KEY = "requestId";
    private static final String MDC_SESSION_ID_KEY = "sessionId";
    private static final String LATITUDE_HEADER = "User-Latitude";
    private static final String LONGITUDE_HEADER = "User-Longitude";
    private static final String CUSTOMER_KEY_HEADER = "User-Customer-Key";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String DEFAULT_ACCOUNT_HEADER = "User-Default-Account";
    private static final String SEGMENTS_HEADER = "User-Segments";
    private static final String SUB_SEGMENTS_HEADER = "User-Sub-Segments";
    private static final String REGION_HEADER = "User-Region";
    private static final String DEVICE_TYPE_HEADER = "Device-Type";
    private static final String APP_VERSION_HEADER = "App-Version";
    private static final String OS_VERSION_HEADER = "OS-Version";
    private static final String DEVICE_ID_HEADER = "Device-Id";

    @Override
    public boolean preHandle(
            HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        String userLang = request.getHeader(LANG_HEADER);
        LocaleContextHolder.setLocale(userLang != null ? userLang : DEFAULT_LANG);
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId != null) {
            MDC.put(MDC_REQUEST_ID_KEY, requestId);
        }
        String sessionId = request.getHeader(SESSION_ID_HEADER);
        if (sessionId != null) {
            MDC.put(MDC_SESSION_ID_KEY, sessionId);
        }

        UserContext userContext = UserContextHolder.getCurrentContext();
        if (userContext == null) {
            userContext = UserContext.builder()
                    .requestId(requestId)
                    .sessionId(sessionId)
                    .build();
        }

        // Set requestId and sessionId
        if (requestId != null && userContext.getRequestId() == null) {
            userContext.setRequestId(requestId);
        }
        if (sessionId != null && userContext.getSessionId() == null) {
            userContext.setSessionId(sessionId);
        }

        // Extract and set all UserContext fields fromEntity headers
        String platform = request.getHeader(PLATFORM_HEADER);
        if (platform != null) {
            userContext.setOsPlatform(platform);
        }
        String appId = request.getHeader(APP_ID_HEADER);
        if (appId != null) {
            userContext.setAppId(appId);
        }
        String appPhoneNumber = request.getHeader(APP_PHONE_NUMBER_HEADER);
        if (appPhoneNumber != null) {
            userContext.setUsername(appPhoneNumber);
        }
        String latitude = request.getHeader(LATITUDE_HEADER);
        if (latitude != null) {
            userContext.setLatitude(latitude);
        }
        String longitude = request.getHeader(LONGITUDE_HEADER);
        if (longitude != null) {
            userContext.setLongitude(longitude);
        }
        String customerKey = request.getHeader(CUSTOMER_KEY_HEADER);
        if (customerKey != null) {
            userContext.setCustomerKey(customerKey);
        }
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId != null) {
            userContext.setUserId(userId);
        }
        String defaultAccount = request.getHeader(DEFAULT_ACCOUNT_HEADER);
        if (defaultAccount != null) {
            userContext.setDefaultAccount(defaultAccount);
        }
        String segments = request.getHeader(SEGMENTS_HEADER);
        if (segments != null) {
            userContext.setSegments(segments);
        }
        String subSegments = request.getHeader(SUB_SEGMENTS_HEADER);
        if (subSegments != null) {
            userContext.setSubSegments(subSegments);
        }
        String region = request.getHeader(REGION_HEADER);
        if (region != null) {
            userContext.setRegion(region);
        }
        String deviceType = request.getHeader(DEVICE_TYPE_HEADER);
        if (deviceType != null) {
            userContext.setDeviceType(deviceType);
        }
        String appVersion = request.getHeader(APP_VERSION_HEADER);
        if (appVersion != null) {
            userContext.setAppVersion(appVersion);
        }
        String osVersion = request.getHeader(OS_VERSION_HEADER);
        if (osVersion != null) {
            userContext.setOsVersion(osVersion);
        }
        String deviceId = request.getHeader(DEVICE_ID_HEADER);
        if (deviceId != null) {
            userContext.setDeviceId(deviceId);
        }

        // Extract IP address fromEntity request
        String ipAddress = getClientIpAddress(request);
        if (ipAddress != null) {
            userContext.setIpAddress(ipAddress);
        }

        UserContextHolder.setUserContext(userContext);
        return true;
    }

    @Override
    public void afterCompletion(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull Object handler,
            Exception ex) {
        LocaleContextHolder.clear();
        MDC.remove(MDC_REQUEST_ID_KEY);
        MDC.remove(MDC_SESSION_ID_KEY);
        UserContextHolder.clearUserContext();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For header
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}
