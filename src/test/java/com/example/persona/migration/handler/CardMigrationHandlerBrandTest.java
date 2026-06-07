package com.example.persona.migration.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CardMigrationHandlerBrandTest {

    @Test
    void inferCardBrandFromType_mapsCommonSchemes() {
        assertThat(CardMigrationHandler.inferCardBrandFromType("VISA")).isEqualTo("VISA");
        assertThat(CardMigrationHandler.inferCardBrandFromType("MC")).isEqualTo("MASTERCARD");
        assertThat(CardMigrationHandler.inferCardBrandFromType("MASTERCARD")).isEqualTo("MASTERCARD");
        assertThat(CardMigrationHandler.inferCardBrandFromType("DEBIT VISA")).isEqualTo("VISA");
    }

    @Test
    void inferCardBrandFromType_fallsBackToRawType() {
        assertThat(CardMigrationHandler.inferCardBrandFromType("LOCAL")).isEqualTo("LOCAL");
    }
}
