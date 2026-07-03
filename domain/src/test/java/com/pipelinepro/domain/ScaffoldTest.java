package com.pipelinepro.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScaffoldTest {

    @Test
    void should_reject_payment_allocation_when_payment_is_cancelled() {
        Payment payment = Payment.received(UUID.randomUUID(), "TX-S1", new BigDecimal("10.00"), "EUR");
        payment.cancel(Instant.now());

        assertThatThrownBy(() -> payment.allocate(new BigDecimal("1.00"), Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }
}
