package com.pipelinepro.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentAllocationTest {

    @Test
    void should_execute_allocation_and_update_payment_and_debt() {
        Instant now = Instant.now();
        Payment payment = Payment.received(UUID.randomUUID(), "TX-AL-1", new BigDecimal("100.00"), "EUR", null, null, null, null, now);
        Debt debt = Debt.open(UUID.randomUUID(), UUID.randomUUID(), "D-AL-1", new BigDecimal("80.00"), "EUR", null, now);

        PaymentAllocation allocation = PaymentAllocation.execute(
                UUID.randomUUID(),
                payment,
                debt,
                UUID.randomUUID(),
                new BigDecimal("60.00"),
                "idem-1",
                "cmd-1",
                "user-a",
                now.plusSeconds(1));

        assertThat(allocation.status()).isEqualTo(AllocationStatus.ALLOCATED);
        assertThat(payment.remainingAmount()).isEqualByComparingTo("40.00");
        assertThat(debt.remainingAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void should_reject_allocation_when_currency_differs() {
        Instant now = Instant.now();
        Payment payment = Payment.received(UUID.randomUUID(), "TX-AL-2", new BigDecimal("100.00"), "EUR", null, null, null, null, now);
        Debt debt = Debt.open(UUID.randomUUID(), UUID.randomUUID(), "D-AL-2", new BigDecimal("80.00"), "USD", null, now);

        assertThatThrownBy(() -> PaymentAllocation.execute(
                UUID.randomUUID(),
                payment,
                debt,
                null,
                new BigDecimal("10.00"),
                "idem-2",
                "cmd-2",
                "user-b",
                now.plusSeconds(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("currencies must match");
    }
}
