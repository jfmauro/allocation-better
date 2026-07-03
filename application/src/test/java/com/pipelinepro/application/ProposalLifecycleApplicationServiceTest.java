package com.pipelinepro.application;

import com.pipelinepro.application.error.ResourceNotFoundException;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.ProposalStatus;
import com.pipelinepro.domain.port.in.command.MarkUnmatchedCommand;
import com.pipelinepro.domain.port.in.command.RejectProposalCommand;
import com.pipelinepro.domain.port.in.command.RequestInvestigationCommand;
import com.pipelinepro.domain.port.in.command.SelectDebtCommand;
import com.pipelinepro.domain.port.in.command.ValidateProposalCommand;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import com.pipelinepro.domain.port.out.AllocationTransactionalWorker;
import com.pipelinepro.domain.port.out.AuditEventGateway;
import com.pipelinepro.domain.port.out.PaymentRepository;
import com.pipelinepro.domain.port.out.command.AllocationExecutionRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProposalLifecycleApplicationServiceTest {

    @Test
    void should_throw_not_found_when_validating_missing_proposal() {
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationTransactionalWorker worker = mock(AllocationTransactionalWorker.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        ProposalLifecycleApplicationService service = new ProposalLifecycleApplicationService(
                proposalRepository,
                paymentRepository,
                worker,
                auditEventGateway);

        UUID proposalId = UUID.randomUUID();
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        ValidateProposalCommand command = new ValidateProposalCommand(
                proposalId,
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "validator",
                "manual-check",
                Instant.parse("2026-04-01T09:00:00Z"));

        assertThatThrownBy(() -> service.validateProposal(command)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_validate_proposal_persist_it_and_trigger_allocation_worker() {
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationTransactionalWorker worker = mock(AllocationTransactionalWorker.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        ProposalLifecycleApplicationService service = new ProposalLifecycleApplicationService(
                proposalRepository,
                paymentRepository,
                worker,
                auditEventGateway);

        Instant now = Instant.parse("2026-04-01T09:00:00Z");
        UUID proposalId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();

        AllocationProposal proposal = AllocationProposal.proposed(proposalId, paymentId, MatchingMethod.IDENTIFIER, "reason", now);
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any(AllocationProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ValidateProposalCommand command = new ValidateProposalCommand(
                proposalId,
                debtId,
                new BigDecimal("10.00"),
                "validator",
                "manual-check",
                now.plusSeconds(5));

        service.validateProposal(command);

        assertThat(proposal.status()).isEqualTo(ProposalStatus.VALIDATED);
        assertThat(proposal.selectedDebtId()).contains(debtId);

        ArgumentCaptor<AllocationExecutionRequest> requestCaptor = ArgumentCaptor.forClass(AllocationExecutionRequest.class);
        verify(worker).executeAllocation(requestCaptor.capture());
        AllocationExecutionRequest request = requestCaptor.getValue();
        assertThat(request.paymentId()).isEqualTo(paymentId);
        assertThat(request.debtId()).isEqualTo(debtId);
        assertThat(request.proposalId()).isEqualTo(proposalId);
        assertThat(request.amount()).isEqualByComparingTo("10.00");
        assertThat(request.idempotencyKey()).isEqualTo("proposal-validation:" + proposalId + ":" + debtId + ":manual-check");
        assertThat(request.commandId()).isEqualTo("validate-proposal:" + proposalId + ":manual-check");
    }

    @Test
    void should_allow_retry_when_proposal_is_already_validated_for_same_debt() {
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationTransactionalWorker worker = mock(AllocationTransactionalWorker.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        ProposalLifecycleApplicationService service = new ProposalLifecycleApplicationService(
                proposalRepository,
                paymentRepository,
                worker,
                auditEventGateway);

        Instant now = Instant.parse("2026-04-01T09:30:00Z");
        UUID proposalId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();

        AllocationProposal proposal = AllocationProposal.proposed(proposalId, paymentId, MatchingMethod.IDENTIFIER, "reason", now);
        proposal.selectDebt("validator", debtId, now.plusSeconds(1));
        proposal.validate("validator", now.plusSeconds(1));

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));

        ValidateProposalCommand command = new ValidateProposalCommand(
                proposalId,
                debtId,
                new BigDecimal("10.00"),
                "validator",
                "manual-check",
                now.plusSeconds(2));

        service.validateProposal(command);

        assertThat(proposal.status()).isEqualTo(ProposalStatus.VALIDATED);
        verify(worker).executeAllocation(any(AllocationExecutionRequest.class));
    }

    @Test
    void should_reject_proposal_by_domain_transition() {
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationTransactionalWorker worker = mock(AllocationTransactionalWorker.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        ProposalLifecycleApplicationService service = new ProposalLifecycleApplicationService(
                proposalRepository,
                paymentRepository,
                worker,
                auditEventGateway);

        Instant now = Instant.parse("2026-04-01T10:00:00Z");
        UUID proposalId = UUID.randomUUID();
        AllocationProposal proposal = AllocationProposal.proposed(proposalId, UUID.randomUUID(), MatchingMethod.NAME, "reason", now);

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any(AllocationProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationProposal saved = service.rejectProposal(new RejectProposalCommand(proposalId, "agent", "invalid", now.plusSeconds(1)));

        assertThat(saved.status()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(saved.reason()).contains("invalid");
        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventGateway).append(auditCaptor.capture());
        assertThat(auditCaptor.getValue().eventType()).isEqualTo("USER_REJECTED_PROPOSAL");
    }

    @Test
    void should_select_debt_on_proposal() {
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationTransactionalWorker worker = mock(AllocationTransactionalWorker.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        ProposalLifecycleApplicationService service = new ProposalLifecycleApplicationService(
                proposalRepository,
                paymentRepository,
                worker,
                auditEventGateway);

        Instant now = Instant.parse("2026-04-01T11:00:00Z");
        UUID proposalId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();
        AllocationProposal proposal = AllocationProposal.proposed(proposalId, UUID.randomUUID(), MatchingMethod.NAME, "reason", now);

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any(AllocationProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationProposal saved = service.selectDebt(new SelectDebtCommand(proposalId, debtId, "agent", now.plusSeconds(1)));
        assertThat(saved.selectedDebtId()).contains(debtId);
    }

    @Test
    void should_mark_unmatched_and_update_payment_state() {
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationTransactionalWorker worker = mock(AllocationTransactionalWorker.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        ProposalLifecycleApplicationService service = new ProposalLifecycleApplicationService(
                proposalRepository,
                paymentRepository,
                worker,
                auditEventGateway);

        Instant now = Instant.parse("2026-04-01T12:00:00Z");
        UUID proposalId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        AllocationProposal proposal = AllocationProposal.proposed(proposalId, paymentId, MatchingMethod.NAME, "reason", now);
        Payment payment = Payment.received(paymentId, "TX-PAY-1", new BigDecimal("20.00"), "EUR", null, null, null, null, now);

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any(AllocationProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationProposal saved = service.markUnmatched(new MarkUnmatchedCommand(proposalId, "agent", "not found", now.plusSeconds(1)));

        assertThat(saved.status()).isEqualTo(ProposalStatus.UNMATCHED);
        assertThat(payment.status().name()).isEqualTo("UNMATCHED");
        verify(paymentRepository).save(payment);
        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventGateway).append(auditCaptor.capture());
        assertThat(auditCaptor.getValue().eventType()).isEqualTo("PAYMENT_MARKED_UNMATCHED");
    }

    @Test
    void should_request_investigation_and_update_payment_state() {
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationTransactionalWorker worker = mock(AllocationTransactionalWorker.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        ProposalLifecycleApplicationService service = new ProposalLifecycleApplicationService(
                proposalRepository,
                paymentRepository,
                worker,
                auditEventGateway);

        Instant now = Instant.parse("2026-04-01T13:00:00Z");
        UUID proposalId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        AllocationProposal proposal = AllocationProposal.proposed(proposalId, paymentId, MatchingMethod.NAME, "reason", now);
        Payment payment = Payment.received(paymentId, "TX-PAY-2", new BigDecimal("20.00"), "EUR", null, null, null, null, now);

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any(AllocationProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationProposal saved = service.requestInvestigation(new RequestInvestigationCommand(proposalId, "agent", "needs check", now.plusSeconds(1)));

        assertThat(saved.status()).isEqualTo(ProposalStatus.INVESTIGATION_REQUESTED);
        assertThat(payment.status().name()).isEqualTo("INVESTIGATION_REQUIRED");
        verify(paymentRepository).save(payment);
        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventGateway).append(auditCaptor.capture());
        assertThat(auditCaptor.getValue().eventType()).isEqualTo("PAYMENT_SENT_TO_INVESTIGATION");
    }
}
