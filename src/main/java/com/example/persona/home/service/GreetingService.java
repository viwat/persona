package com.example.persona.home.service;

import com.example.persona.home.dto.GreetingResponse;
import com.example.persona.i18n.service.TranslationService;
import com.example.persona.profile.model.CustomerProfile;
import com.example.persona.profile.repository.CustomerProfileRepository;
import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Translation codes used for greeting (add to dgtl_translation with name_en,
 * name_km, name_zh): GREETING_MORNING, GREETING_AFTERNOON, GREETING_EVENING,
 * GREETING_NIGHT, GREETING_HELLO, GREETING_FULL_FORMAT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GreetingService {

    private static final String CODE_MORNING = "GREETING_MORNING";
    private static final String CODE_AFTERNOON = "GREETING_AFTERNOON";
    private static final String CODE_EVENING = "GREETING_EVENING";
    private static final String CODE_NIGHT = "GREETING_NIGHT";
    private static final String CODE_HELLO = "GREETING_HELLO";
    private static final String CODE_FULL_FORMAT = "GREETING_FULL_FORMAT";
    private static final String DEFAULT_FULL_FORMAT = "Hey Hey, %s %s";

    private final CustomerProfileRepository customerProfileRepository;
    private final TranslationService translationService;

    @Transactional(readOnly = true)
    public GreetingResponse getGreeting(String customerNo) {
        log.debug("Getting greeting for customer: {}", customerNo);

        // Get customer name
        String userName = getCustomerName(customerNo);

        // Determine time of day and get translated greeting text
        String timeOfDay = getTimeOfDay();
        String greetingText = getTranslatedGreeting(timeOfDay);

        // Build full greeting using translated format string
        String fullGreeting = buildFullGreeting(greetingText, userName);

        return GreetingResponse.builder()
                .greeting(greetingText)
                .userName(userName)
                .timeOfDay(timeOfDay)
                .fullGreeting(fullGreeting)
                .build();
    }

    private String getCustomerName(String customerNo) {
        Optional<CustomerProfile> profileOpt = customerProfileRepository.findByCustomerNo(customerNo);
        if (profileOpt.isPresent() && profileOpt.get().getCustomerName() != null) {
            return profileOpt.get().getCustomerName();
        }
        // Fallback to customer number if name not found
        return customerNo;
    }

    private String getTimeOfDay() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        if (hour >= 5 && hour < 12) {
            return "MORNING";
        } else if (hour >= 12 && hour < 17) {
            return "AFTERNOON";
        } else if (hour >= 17 && hour < 21) {
            return "EVENING";
        } else {
            return "NIGHT";
        }
    }

    private String getGreetingCode(String timeOfDay) {
        return switch (timeOfDay) {
            case "MORNING" -> CODE_MORNING;
            case "AFTERNOON" -> CODE_AFTERNOON;
            case "EVENING" -> CODE_EVENING;
            case "NIGHT" -> CODE_NIGHT;
            default -> CODE_HELLO;
        };
    }

    private String getGreetingTextFallback(String timeOfDay) {
        return switch (timeOfDay) {
            case "MORNING" -> "Good Morning";
            case "AFTERNOON" -> "Good Afternoon";
            case "EVENING" -> "Good Evening";
            case "NIGHT" -> "Good Night";
            default -> "Hello";
        };
    }

    /**
     * Returns translated greeting for current locale; falls back to English if
     * translation not found.
     */
    private String getTranslatedGreeting(String timeOfDay) {
        String code = getGreetingCode(timeOfDay);
        return translationService.getTranslationOrDefault(code, getGreetingTextFallback(timeOfDay));
    }

    private String getFullGreetingFormat() {
        return translationService.getTranslationOrDefault(CODE_FULL_FORMAT, DEFAULT_FULL_FORMAT);
    }

    private String buildFullGreeting(String greeting, String userName) {
        return String.format(getFullGreetingFormat(), greeting, userName);
    }
}
