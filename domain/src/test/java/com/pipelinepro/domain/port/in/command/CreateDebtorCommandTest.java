package com.pipelinepro.domain.port.in.command;

import com.pipelinepro.domain.DebtorType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateDebtorCommandTest {

    @Test
    void should_create_natural_person_command_with_required_metadata() {
        CreateDebtorCommand command = new CreateDebtorCommand(
                DebtorType.NATURAL_PERSON,
                "John Doe",
                "85073003328",
                null,
                "idem-123",
                "corr-123");

        assertThat(command.debtorType()).isEqualTo(DebtorType.NATURAL_PERSON);
        assertThat(command.idempotencyKey()).isEqualTo("idem-123");
        assertThat(command.correlationId()).isEqualTo("corr-123");
    }

    @Test
    void should_reject_invalid_debtor_command_shapes() {
        assertThatThrownBy(() -> new CreateDebtorCommand(
                DebtorType.NATURAL_PERSON,
                "John Doe",
                null,
                null,
                "idem-123",
                "corr-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires nationalNumber");

        assertThatThrownBy(() -> new CreateDebtorCommand(
                DebtorType.ENTERPRISE,
                "Acme",
                null,
                "BE0000000000",
                " ",
                "corr-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotencyKey must be non-blank");
    }
}
