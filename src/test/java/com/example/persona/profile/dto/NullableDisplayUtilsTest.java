package com.example.persona.profile.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.persona.utils.NullableDisplayUtils;
import org.junit.jupiter.api.Test;

class NullableDisplayUtilsTest {

    @Test
    void stringOrNa_returnsLiteralForNonNull() {
        assertThat(NullableDisplayUtils.stringOrNa("x")).isEqualTo("x");
    }

    @Test
    void stringOrNa_returnsNaForNull() {
        assertThat(NullableDisplayUtils.stringOrNa(null)).isEqualTo(NullableDisplayUtils.NOT_AVAILABLE);
    }

    @Test
    void stringOrNa_returnsNaForBlank() {
        assertThat(NullableDisplayUtils.stringOrNa("")).isEqualTo(NullableDisplayUtils.NOT_AVAILABLE);
        assertThat(NullableDisplayUtils.stringOrNa("   ")).isEqualTo(NullableDisplayUtils.NOT_AVAILABLE);
    }
}
