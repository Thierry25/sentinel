package com.sentinel.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountTest {

    @Test
    void shouldTrowErrorOnEmptyIdProvided() {
        assertThatThrownBy(() -> new Account("", Desk.EQUITIES))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowErrorOnNullIdProvided() {
        assertThatThrownBy(() -> new Account(null, Desk.FX))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
