package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.port.out.command.PublishIntakeAuditEventCommand;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublishIntakeAuditEventCommandTest {

    @Test
    void should_build_intake_event_type_with_lifecycle_suffixes_only() {
        PublishIntakeAuditEventCommand command = new PublishIntakeAuditEventCommand(
                UUID.randomUUID(),
                IntakeAggregateType.DEBTOR,
                UUID.randomUUID(),
                IntakeAuditLifecycle.REQUESTED,
                null,
                "corr-123",
                Instant.now());

        assertThat(command.eventType()).isEqualTo("DEBTOR_REQUESTED");
    }

    @Test
    void should_require_reason_code_only_for_rejected_events() {
        assertThatThrownBy(() -> new PublishIntakeAuditEventCommand(
                UUID.randomUUID(),
                IntakeAggregateType.DEBT,
                UUID.randomUUID(),
                IntakeAuditLifecycle.REJECTED,
                null,
                "corr-123",
                Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reasonCode must be provided");

        assertThatThrownBy(() -> new PublishIntakeAuditEventCommand(
                UUID.randomUUID(),
                IntakeAggregateType.DEBT,
                UUID.randomUUID(),
                IntakeAuditLifecycle.CREATED,
                "DUPLICATE",
                "corr-123",
                Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only allowed for REJECTED");
    }
}
