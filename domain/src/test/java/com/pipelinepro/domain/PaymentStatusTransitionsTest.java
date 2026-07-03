package com.pipelinepro.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentStatusTransitionsTest {

    @Test
    void should_transition_to_match_unmatched_and_investigation() {
        Payment payment = Payment.received(UUID.randomUUID(), "TX-STS-1", new BigDecimal("25.00"), "EUR");

        payment.markToMatch(Instant.now());
        assertThat(payment.status()).isEqualTo(PaymentStatus.TO_MATCH);

        payment.markUnmatched(Instant.now());
        assertThat(payment.status()).isEqualTo(PaymentStatus.UNMATCHED);

        payment.requestInvestigation(Instant.now());
        assertThat(payment.status()).isEqualTo(PaymentStatus.INVESTIGATION_REQUIRED);
    }
}
