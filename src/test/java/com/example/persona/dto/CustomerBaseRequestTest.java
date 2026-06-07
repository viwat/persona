package com.example.persona.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomerBaseRequestTest {

    @Test
    void deriveCustomerNoReturnsFirstSegmentWhenUnderscorePresent() {
        assertThat(CustomerBaseRequest.deriveCustomerNo("002948016_102815211")).isEqualTo("002948016");
    }

    @Test
    void deriveCustomerNoReturnsWholeKeyWhenNoUnderscore() {
        assertThat(CustomerBaseRequest.deriveCustomerNo("002948016")).isEqualTo("002948016");
    }

    @Test
    void deriveCustomerNoHandlesThreePartKey() {
        assertThat(CustomerBaseRequest.deriveCustomerNo("002948016_102815211_MST012"))
                .isEqualTo("002948016");
    }
}
