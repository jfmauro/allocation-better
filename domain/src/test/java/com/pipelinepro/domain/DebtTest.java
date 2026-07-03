package com.pipelinepro.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebtTest {

    @Test
    void should_create_open_debt_with_due_date() {
        Debt debt = Debt.open(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "D-2026-1",
                new BigDecimal("200.00"),
                "EUR",
                LocalDate.of(2026, 7, 10),
                Instant.now());

        assertThat(debt.status()).isEqualTo(DebtStatus.OPEN);
        assertThat(debt.remainingAmount()).isEqualByComparingTo("200.00");
        assertThat(debt.dueDate()).contains(LocalDate.of(2026, 7, 10));
    }

    @Test
    void should_transition_to_partially_paid_and_paid() {
        Instant now = Instant.now();
        Debt debt = Debt.open(UUID.randomUUID(), UUID.randomUUID(), "D-2026-2", new BigDecimal("50.00"), "EUR", null, now);

        debt.pay(new BigDecimal("20.00"), now.plusSeconds(1));
        assertThat(debt.status()).isEqualTo(DebtStatus.PARTIALLY_PAID);

        debt.pay(new BigDecimal("30.00"), now.plusSeconds(2));
        assertThat(debt.status()).isEqualTo(DebtStatus.PAID);
    }

    @Test
    void should_reject_overpayment() {
        Debt debt = Debt.open(UUID.randomUUID(), UUID.randomUUID(), "D-2026-3", new BigDecimal("10.00"), "EUR");

        assertThatThrownBy(() -> debt.pay(new BigDecimal("12.00"), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot pay more than remainingAmount");
    }
}
