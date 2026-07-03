package com.pipelinepro.application;

import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.DebtorType;
import com.pipelinepro.application.error.DuplicateResourceException;
import com.pipelinepro.application.port.out.DebtorIntakeWorker;
import com.pipelinepro.domain.port.in.command.CreateDebtorCommand;
import com.pipelinepro.domain.port.out.IntakeAggregateType;
import com.pipelinepro.domain.port.out.IntakeAuditEventGateway;
import com.pipelinepro.domain.port.out.IntakeAuditLifecycle;
import com.pipelinepro.domain.port.out.command.PublishIntakeAuditEventCommand;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateDebtorIntakeApplicationServiceTest {

    @Test
    void should_reject_when_command_is_null_inside_audited_flow() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        assertThatThrownBy(() -> service.createDebtor(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("command must be non-null");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway).publish(auditCaptor.capture());
        assertThat(auditCaptor.getValue().lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getValue().reasonCode()).isEqualTo("VALIDATION");
        assertThat(auditCaptor.getValue().correlationId()).isEqualTo("UNKNOWN");
        verify(debtorIntakeWorker, never()).createDebtor(any());
    }

    @Test
    void should_reject_when_idempotency_key_is_blank_inside_audited_flow() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        CreateDebtorCommand command = mock(CreateDebtorCommand.class);
        when(command.idempotencyKey()).thenReturn("   ");
        when(command.correlationId()).thenReturn("corr-1");

        assertThatThrownBy(() -> service.createDebtor(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotencyKey must be non-blank");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway).publish(auditCaptor.capture());
        assertThat(auditCaptor.getValue().lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getValue().reasonCode()).isEqualTo("VALIDATION");
        verify(debtorIntakeWorker, never()).createDebtor(any());
    }

    @Test
    void should_create_natural_person_and_publish_requested_then_created() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        CreateDebtorCommand command = new CreateDebtorCommand(
                DebtorType.NATURAL_PERSON,
                "Alice Doe",
                "85073003328",
                null,
                "idem-1",
                "corr-1");

        Debtor savedDebtor = Debtor.activeNaturalPerson(
                java.util.UUID.randomUUID(),
                "Alice Doe",
                "85073003328",
                java.time.Instant.now());
        when(debtorIntakeWorker.createDebtor(command)).thenReturn(savedDebtor);

        Debtor debtor = service.createDebtor(command);

        assertThat(debtor.type()).isEqualTo(DebtorType.NATURAL_PERSON);
        assertThat(debtor.displayName()).isEqualTo("Alice Doe");
        assertThat(debtor.nationalNumber()).contains("85073003328");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());

        PublishIntakeAuditEventCommand requested = auditCaptor.getAllValues().get(0);
        PublishIntakeAuditEventCommand created = auditCaptor.getAllValues().get(1);

        assertThat(requested.aggregateType()).isEqualTo(IntakeAggregateType.DEBTOR);
        assertThat(requested.lifecycle()).isEqualTo(IntakeAuditLifecycle.REQUESTED);
        assertThat(requested.reasonCode()).isNull();
        assertThat(created.aggregateType()).isEqualTo(IntakeAggregateType.DEBTOR);
        assertThat(created.lifecycle()).isEqualTo(IntakeAuditLifecycle.CREATED);
        assertThat(created.reasonCode()).isNull();
        assertThat(requested.aggregateId())
                .isEqualTo(java.util.UUID.nameUUIDFromBytes(command.idempotencyKey().getBytes(StandardCharsets.UTF_8)));
        assertThat(created.aggregateId()).isEqualTo(savedDebtor.id());

        InOrder inOrder = inOrder(intakeAuditEventGateway, debtorIntakeWorker);
        inOrder.verify(intakeAuditEventGateway).publish(any(PublishIntakeAuditEventCommand.class));
        inOrder.verify(debtorIntakeWorker).createDebtor(command);
        inOrder.verify(intakeAuditEventGateway).publish(any(PublishIntakeAuditEventCommand.class));
    }

    @Test
    void should_reject_duplicate_with_duplicate_reason() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        CreateDebtorCommand command = new CreateDebtorCommand(
                DebtorType.NATURAL_PERSON,
                "Alice Doe",
                "85073003328",
                null,
                "idem-1",
                "corr-1");

        when(debtorIntakeWorker.createDebtor(command))
                .thenThrow(new DuplicateResourceException("Duplicate debtor"));

        assertThatThrownBy(() -> service.createDebtor(command))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Duplicate debtor");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(0).lifecycle()).isEqualTo(IntakeAuditLifecycle.REQUESTED);
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("DUPLICATE");

        InOrder inOrder = inOrder(intakeAuditEventGateway, debtorIntakeWorker);
        inOrder.verify(intakeAuditEventGateway).publish(any(PublishIntakeAuditEventCommand.class));
        inOrder.verify(debtorIntakeWorker).createDebtor(command);
        inOrder.verify(intakeAuditEventGateway).publish(any(PublishIntakeAuditEventCommand.class));
    }

    @Test
    void should_reject_with_validation_reason_when_worker_fails_validation() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        CreateDebtorCommand command = new CreateDebtorCommand(
                DebtorType.ENTERPRISE,
                "Acme",
                null,
                "BE0123456789",
                "idem-1",
                "corr-1");

        when(debtorIntakeWorker.createDebtor(command))
                .thenThrow(new IllegalArgumentException("validation failed"));

        assertThatThrownBy(() -> service.createDebtor(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("validation failed");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("VALIDATION");
    }

    @Test
    void should_reject_with_technical_reason_when_unexpected_failure_occurs() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        CreateDebtorCommand command = new CreateDebtorCommand(
                DebtorType.ENTERPRISE,
                "Acme",
                null,
                "BE0123456789",
                "idem-1",
                "corr-1");

        when(debtorIntakeWorker.createDebtor(command)).thenThrow(new RuntimeException("database down"));

        assertThatThrownBy(() -> service.createDebtor(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("database down");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("TECHNICAL");
    }

    @Test
    void should_reject_illegal_state_with_technical_reason() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        CreateDebtorCommand command = new CreateDebtorCommand(
                DebtorType.ENTERPRISE,
                "Acme",
                null,
                "BE0123456789",
                "idem-1",
                "corr-1");

        when(debtorIntakeWorker.createDebtor(command))
                .thenThrow(new IllegalStateException("unexpected illegal state"));

        assertThatThrownBy(() -> service.createDebtor(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unexpected illegal state");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("TECHNICAL");
    }

    @Test
    void should_fail_when_requested_audit_publish_fails() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        CreateDebtorCommand command = new CreateDebtorCommand(
                DebtorType.NATURAL_PERSON,
                "Alice Doe",
                "85073003328",
                null,
                "idem-1",
                "corr-1");

        doThrow(new RuntimeException("audit unavailable"))
                .doNothing()
                .when(intakeAuditEventGateway)
                .publish(any(PublishIntakeAuditEventCommand.class));

        assertThatThrownBy(() -> service.createDebtor(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("audit unavailable");

        verify(debtorIntakeWorker, never()).createDebtor(command);
        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(0).lifecycle()).isEqualTo(IntakeAuditLifecycle.REQUESTED);
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("TECHNICAL");
    }

    @Test
    void should_fail_with_technical_error_when_rejected_audit_publish_fails() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        CreateDebtorCommand command = new CreateDebtorCommand(
                DebtorType.NATURAL_PERSON,
                "Alice Doe",
                "85073003328",
                null,
                "idem-1",
                "corr-1");

        when(debtorIntakeWorker.createDebtor(command))
                .thenThrow(new DuplicateResourceException("Duplicate debtor"));
        doNothing()
                .doThrow(new RuntimeException("audit unavailable"))
                .when(intakeAuditEventGateway)
                .publish(any(PublishIntakeAuditEventCommand.class));

        assertThatThrownBy(() -> service.createDebtor(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("audit unavailable");

        verify(intakeAuditEventGateway, times(2)).publish(any(PublishIntakeAuditEventCommand.class));
    }

    @Test
    void should_fail_when_created_audit_publish_fails_after_worker_success() {
        DebtorIntakeWorker debtorIntakeWorker = mock(DebtorIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtorIntakeApplicationService service =
                new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);

        CreateDebtorCommand command = new CreateDebtorCommand(
                DebtorType.NATURAL_PERSON,
                "Alice Doe",
                "85073003328",
                null,
                "idem-1",
                "corr-1");

        Debtor savedDebtor = Debtor.activeNaturalPerson(
                java.util.UUID.randomUUID(),
                "Alice Doe",
                "85073003328",
                java.time.Instant.now());
        when(debtorIntakeWorker.createDebtor(command)).thenReturn(savedDebtor);
        doNothing()
                .doThrow(new RuntimeException("audit unavailable"))
                .when(intakeAuditEventGateway)
                .publish(any(PublishIntakeAuditEventCommand.class));

        assertThatThrownBy(() -> service.createDebtor(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("audit unavailable");

        verify(debtorIntakeWorker).createDebtor(command);
        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(3)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(0).lifecycle()).isEqualTo(IntakeAuditLifecycle.REQUESTED);
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.CREATED);
        assertThat(auditCaptor.getAllValues().get(2).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(2).reasonCode()).isEqualTo("TECHNICAL");
    }
}
