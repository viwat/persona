package com.example.persona.profile.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CardAppProfileServiceParseTest {

    @Test
    void parseCardExpiryString_acceptsIsoDate() {
        assertThat(CardAppProfileService.parseCardExpiryString("2026-07-27")).isEqualTo(LocalDate.of(2026, 7, 27));
    }

    @Test
    void parseCardExpiryString_acceptsLegacyMonthDay() {
        assertThat(CardAppProfileService.parseCardExpiryString("07/27")).isNotNull();
        assertThat(CardAppProfileService.parseCardExpiryString("07/27").getMonthValue())
                .isEqualTo(7);
        assertThat(CardAppProfileService.parseCardExpiryString("07/27").getDayOfMonth())
                .isEqualTo(27);
    }

    @Test
    void lastFourDigitsFromPan_stripsNonDigits() {
        assertThat(CardAppProfileService.lastFourDigitsFromPan("288103100148426"))
                .isEqualTo("8426");
        assertThat(CardAppProfileService.lastFourDigitsFromPan("2881-0310-0148-426"))
                .isEqualTo("8426");
    }

    @Test
    void lastFourDigitsFromPan_returnsNullForShortInput() {
        assertThat(CardAppProfileService.lastFourDigitsFromPan("123")).isNull();
        assertThat(CardAppProfileService.lastFourDigitsFromPan(null)).isNull();
    }
}
