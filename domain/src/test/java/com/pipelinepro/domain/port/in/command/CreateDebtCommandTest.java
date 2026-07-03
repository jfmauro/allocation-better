package com.pipelinepro.domain.port.in.command;

import com.pipelinepro.domain.DebtStatus;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateDebtCommandTest {

    @Test
    void should_accept_allocatable_opening_status_and_metadata() {
        CreateDebtCommand command = new CreateDebtCommand(
                UUID.randomUUID(),
                "DEBT-REF-001",
                new BigDecimal("100.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-123",
                "corr-123");

        assertThat(command.openingStatus()).isEqualTo(DebtStatus.OPEN);
    }

    @Test
    void should_reject_non_allocatable_status_and_invalid_metadata() {
        assertThatThrownBy(() -> new CreateDebtCommand(
                UUID.randomUUID(),
                "DEBT-REF-001",
                new BigDecimal("100.00"),
                "EUR",
                DebtStatus.PAID,
                null,
                "idem-123",
                "corr-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("allocatable");

        assertThatThrownBy(() -> new CreateDebtCommand(
                UUID.randomUUID(),
                "DEBT-REF-001",
                new BigDecimal("0"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-123",
                "corr-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("originalAmount must be > 0");
    }
}
