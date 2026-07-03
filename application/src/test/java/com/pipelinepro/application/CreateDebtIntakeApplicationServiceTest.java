package com.pipelinepro.application;

import com.pipelinepro.application.error.ResourceNotFoundException;
import com.pipelinepro.application.error.DuplicateResourceException;
import com.pipelinepro.application.error.ReferencedResourceNotFoundException;
import com.pipelinepro.application.port.out.DebtIntakeWorker;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.port.in.command.CreateDebtCommand;
import com.pipelinepro.domain.port.out.IntakeAggregateType;
import com.pipelinepro.domain.port.out.IntakeAuditEventGateway;
import com.pipelinepro.domain.port.out.IntakeAuditLifecycle;
import com.pipelinepro.domain.port.out.command.PublishIntakeAuditEventCommand;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
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

class CreateDebtIntakeApplicationServiceTest {

    @Test
    void should_reject_when_command_is_null_inside_audited_flow() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        assertThatThrownBy(() -> service.createDebt(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("command must be non-null");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway).publish(auditCaptor.capture());
        assertThat(auditCaptor.getValue().lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getValue().reasonCode()).isEqualTo("VALIDATION");
        assertThat(auditCaptor.getValue().correlationId()).isEqualTo("UNKNOWN");
        verify(debtIntakeWorker, never()).createDebt(any());
    }

    @Test
    void should_reject_when_idempotency_key_is_blank_inside_audited_flow() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        CreateDebtCommand command = mock(CreateDebtCommand.class);
        when(command.idempotencyKey()).thenReturn("   ");
        when(command.correlationId()).thenReturn("corr-1");

        assertThatThrownBy(() -> service.createDebt(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotencyKey must be non-blank");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway).publish(auditCaptor.capture());
        assertThat(auditCaptor.getValue().lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getValue().reasonCode()).isEqualTo("VALIDATION");
        verify(debtIntakeWorker, never()).createDebt(any());
    }

    @Test
    void should_create_debt_and_publish_requested_then_created() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        UUID debtorId = UUID.randomUUID();
        CreateDebtCommand command = new CreateDebtCommand(
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1");

        Debt savedDebt = new Debt(
                UUID.randomUUID(),
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                new BigDecimal("120.00"),
                0L,
                java.time.Instant.now(),
                java.time.Instant.now());
        when(debtIntakeWorker.createDebt(command)).thenReturn(savedDebt);

        Debt debt = service.createDebt(command);

        assertThat(debt.debtorId()).isEqualTo(debtorId);
        assertThat(debt.status()).isEqualTo(DebtStatus.OPEN);
        assertThat(debt.remainingAmount()).isEqualByComparingTo("120.00");
        assertThat(debt.version()).isZero();

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());

        PublishIntakeAuditEventCommand requested = auditCaptor.getAllValues().get(0);
        PublishIntakeAuditEventCommand created = auditCaptor.getAllValues().get(1);

        assertThat(requested.aggregateType()).isEqualTo(IntakeAggregateType.DEBT);
        assertThat(requested.lifecycle()).isEqualTo(IntakeAuditLifecycle.REQUESTED);
        assertThat(requested.reasonCode()).isNull();
        assertThat(created.aggregateType()).isEqualTo(IntakeAggregateType.DEBT);
        assertThat(created.lifecycle()).isEqualTo(IntakeAuditLifecycle.CREATED);
        assertThat(created.reasonCode()).isNull();
        assertThat(requested.aggregateId())
                .isEqualTo(UUID.nameUUIDFromBytes(command.idempotencyKey().getBytes(StandardCharsets.UTF_8)));
        assertThat(created.aggregateId()).isEqualTo(savedDebt.id());

        InOrder inOrder = inOrder(intakeAuditEventGateway, debtIntakeWorker);
        inOrder.verify(intakeAuditEventGateway).publish(any(PublishIntakeAuditEventCommand.class));
        inOrder.verify(debtIntakeWorker).createDebt(command);
        inOrder.verify(intakeAuditEventGateway).publish(any(PublishIntakeAuditEventCommand.class));
    }

    @Test
    void should_reject_with_not_found_when_debtor_does_not_exist() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        UUID debtorId = UUID.randomUUID();
        CreateDebtCommand command = new CreateDebtCommand(
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1");

        when(debtIntakeWorker.createDebt(command))
                .thenThrow(new ReferencedResourceNotFoundException("Debtor not found: " + debtorId));

        assertThatThrownBy(() -> service.createDebt(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Referenced resource not found")
                .hasCauseInstanceOf(ReferencedResourceNotFoundException.class);

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void should_reject_duplicate_reference_with_duplicate_reason() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        UUID debtorId = UUID.randomUUID();
        CreateDebtCommand command = new CreateDebtCommand(
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1");

        when(debtIntakeWorker.createDebt(command))
                .thenThrow(new DuplicateResourceException("Duplicate debt reference: INV-100"));

        assertThatThrownBy(() -> service.createDebt(command))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Duplicate debt reference");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("DUPLICATE");
    }

    @Test
    void should_reject_with_validation_reason_when_save_fails_validation() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        UUID debtorId = UUID.randomUUID();
        CreateDebtCommand command = new CreateDebtCommand(
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1");

        when(debtIntakeWorker.createDebt(command)).thenThrow(new IllegalArgumentException("validation failed"));

        assertThatThrownBy(() -> service.createDebt(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("validation failed");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("VALIDATION");
    }

    @Test
    void should_reject_with_technical_reason_when_unexpected_failure_occurs() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        UUID debtorId = UUID.randomUUID();
        CreateDebtCommand command = new CreateDebtCommand(
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1");

        when(debtIntakeWorker.createDebt(command)).thenThrow(new RuntimeException("storage down"));

        assertThatThrownBy(() -> service.createDebt(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("storage down");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("TECHNICAL");
    }

    @Test
    void should_reject_illegal_state_with_technical_reason() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        UUID debtorId = UUID.randomUUID();
        CreateDebtCommand command = new CreateDebtCommand(
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1");

        when(debtIntakeWorker.createDebt(command))
                .thenThrow(new IllegalStateException("unexpected illegal state"));

        assertThatThrownBy(() -> service.createDebt(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unexpected illegal state");

        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("TECHNICAL");
    }

    @Test
    void should_fail_when_requested_audit_publish_fails() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        UUID debtorId = UUID.randomUUID();
        CreateDebtCommand command = new CreateDebtCommand(
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1");

        doThrow(new RuntimeException("audit unavailable"))
                .doNothing()
                .when(intakeAuditEventGateway)
                .publish(any(PublishIntakeAuditEventCommand.class));

        assertThatThrownBy(() -> service.createDebt(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("audit unavailable");

        verify(debtIntakeWorker, never()).createDebt(command);
        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(2)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(0).lifecycle()).isEqualTo(IntakeAuditLifecycle.REQUESTED);
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(1).reasonCode()).isEqualTo("TECHNICAL");
    }

    @Test
    void should_fail_with_technical_error_when_rejected_audit_publish_fails() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        UUID debtorId = UUID.randomUUID();
        CreateDebtCommand command = new CreateDebtCommand(
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1");

        when(debtIntakeWorker.createDebt(command))
                .thenThrow(new DuplicateResourceException("Duplicate debt reference: INV-100"));
        doNothing()
                .doThrow(new RuntimeException("audit unavailable"))
                .when(intakeAuditEventGateway)
                .publish(any(PublishIntakeAuditEventCommand.class));

        assertThatThrownBy(() -> service.createDebt(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("audit unavailable");

        verify(intakeAuditEventGateway, times(2)).publish(any(PublishIntakeAuditEventCommand.class));
    }

    @Test
    void should_fail_when_created_audit_publish_fails_after_worker_success() {
        DebtIntakeWorker debtIntakeWorker = mock(DebtIntakeWorker.class);
        IntakeAuditEventGateway intakeAuditEventGateway = mock(IntakeAuditEventGateway.class);
        CreateDebtIntakeApplicationService service =
                new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);

        UUID debtorId = UUID.randomUUID();
        CreateDebtCommand command = new CreateDebtCommand(
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1");

        Debt savedDebt = new Debt(
                UUID.randomUUID(),
                debtorId,
                "INV-100",
                new BigDecimal("120.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                new BigDecimal("120.00"),
                0L,
                java.time.Instant.now(),
                java.time.Instant.now());
        when(debtIntakeWorker.createDebt(command)).thenReturn(savedDebt);
        doNothing()
                .doThrow(new RuntimeException("audit unavailable"))
                .when(intakeAuditEventGateway)
                .publish(any(PublishIntakeAuditEventCommand.class));

        assertThatThrownBy(() -> service.createDebt(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("audit unavailable");

        verify(debtIntakeWorker).createDebt(command);
        ArgumentCaptor<PublishIntakeAuditEventCommand> auditCaptor = ArgumentCaptor.forClass(PublishIntakeAuditEventCommand.class);
        verify(intakeAuditEventGateway, times(3)).publish(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().get(0).lifecycle()).isEqualTo(IntakeAuditLifecycle.REQUESTED);
        assertThat(auditCaptor.getAllValues().get(1).lifecycle()).isEqualTo(IntakeAuditLifecycle.CREATED);
        assertThat(auditCaptor.getAllValues().get(2).lifecycle()).isEqualTo(IntakeAuditLifecycle.REJECTED);
        assertThat(auditCaptor.getAllValues().get(2).reasonCode()).isEqualTo("TECHNICAL");
    }
}
