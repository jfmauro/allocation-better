package com.pipelinepro.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Test
    void should_create_received_payment_with_full_remaining_amount() {
        Instant now = Instant.now();
        Payment payment = Payment.received(
                UUID.randomUUID(),
                "TX-001",
                new BigDecimal("120.00"),
                "EUR",
                "+++123/4567/89002+++",
                "invoice 2026-001",
                "John Doe",
                "BE68********1234",
                now);

        assertThat(payment.status()).isEqualTo(PaymentStatus.RECEIVED);
        assertThat(payment.remainingAmount()).isEqualByComparingTo("120.00");
        assertThat(payment.structuredCommunication()).contains("+++123/4567/89002+++");
    }

    @Test
    void should_update_status_when_allocating_payment() {
        Instant now = Instant.now();
        Payment payment = Payment.received(UUID.randomUUID(), "TX-002", new BigDecimal("100.00"), "EUR", null, null, null, null, now);

        payment.allocate(new BigDecimal("40.00"), now.plusSeconds(1));
        assertThat(payment.status()).isEqualTo(PaymentStatus.PARTIALLY_ALLOCATED);
        assertThat(payment.remainingAmount()).isEqualByComparingTo("60.00");

        payment.allocate(new BigDecimal("60.00"), now.plusSeconds(2));
        assertThat(payment.status()).isEqualTo(PaymentStatus.ALLOCATED);
        assertThat(payment.remainingAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void should_reject_allocation_bigger_than_remaining_amount() {
        Payment payment = Payment.received(UUID.randomUUID(), "TX-003", new BigDecimal("10.00"), "EUR");

        assertThatThrownBy(() -> payment.allocate(new BigDecimal("10.01"), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot allocate more than remainingAmount");
    }
}
