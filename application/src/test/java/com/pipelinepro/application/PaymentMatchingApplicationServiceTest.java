package com.pipelinepro.application;

import com.pipelinepro.application.error.ResourceNotFoundException;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.MatchConfidence;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentStatus;
import com.pipelinepro.domain.port.in.command.MatchPaymentCommand;
import com.pipelinepro.domain.port.in.result.MatchPaymentResult;
import com.pipelinepro.domain.port.out.AllocationProposalCandidateRepository;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import com.pipelinepro.domain.port.out.AllocationTransactionalWorker;
import com.pipelinepro.domain.port.out.AuditEventGateway;
import com.pipelinepro.domain.port.out.DebtRepository;
import com.pipelinepro.domain.port.out.DebtorRepository;
import com.pipelinepro.domain.port.out.PaymentRepository;
import com.pipelinepro.domain.port.out.command.AllocationExecutionRequest;
import com.pipelinepro.domain.service.NameMatchScorer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentMatchingApplicationServiceTest {

    @Test
    void should_throw_not_found_when_payment_is_missing() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        AllocationProposalCandidateRepository candidateRepository = mock(AllocationProposalCandidateRepository.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        AllocationTransactionalWorker allocationTransactionalWorker = mock(AllocationTransactionalWorker.class);

        PaymentMatchingApplicationService service = new PaymentMatchingApplicationService(
                paymentRepository,
                debtorRepository,
                debtRepository,
                proposalRepository,
                candidateRepository,
                auditEventGateway,
                allocationTransactionalWorker);

        UUID paymentId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-10T08:00:00Z");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.matchPayment(new MatchPaymentCommand(paymentId, MatchingMethod.NAME, now)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_honor_priority_and_stop_at_identifier_when_name_requested() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        AllocationProposalCandidateRepository candidateRepository = mock(AllocationProposalCandidateRepository.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        AllocationTransactionalWorker allocationTransactionalWorker = mock(AllocationTransactionalWorker.class);

        PaymentMatchingApplicationService service = new PaymentMatchingApplicationService(
                paymentRepository,
                debtorRepository,
                debtRepository,
                proposalRepository,
                candidateRepository,
                auditEventGateway,
                allocationTransactionalWorker);

        Instant now = Instant.parse("2026-03-10T09:00:00Z");
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.received(
                paymentId,
                "TX-MATCH-1",
                new BigDecimal("150.00"),
                "EUR",
                "+++111/1111/11111+++",
                "Customer data BE0820501224",
                "Acme",
                null,
                now);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Debtor debtor = Debtor.activeEnterprise(UUID.randomUUID(), "Acme", "0820501224", now);
        Debt debt = Debt.open(UUID.randomUUID(), debtor.id(), "DEBT-1", new BigDecimal("90.00"), "EUR", null, now);

        when(debtorRepository.findByEnterpriseNumber("0820501224")).thenReturn(Optional.of(debtor));
        when(debtRepository.findByDebtorIds(anySet())).thenReturn(List.of(debt));
        when(proposalRepository.save(any(AllocationProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(candidateRepository.save(any(AllocationProposalCandidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchPaymentResult result = service.matchPayment(new MatchPaymentCommand(paymentId, MatchingMethod.NAME, now));

        assertThat(result.matchingMethod()).isEqualTo(MatchingMethod.IDENTIFIER);
        assertThat(result.autoAllocationExecuted()).isFalse();
        assertThat(result.proposalId()).isPresent();
        assertThat(payment.status()).isEqualTo(PaymentStatus.MATCH_PROPOSED);
        verify(candidateRepository).save(any(AllocationProposalCandidate.class));
        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventGateway, times(5)).append(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().stream().map(AuditEvent::eventType).toList())
                .contains(
                        "STRUCTURED_COMMUNICATION_NORMALIZED",
                        "STRUCTURED_COMMUNICATION_REJECTED",
                        "IDENTIFIER_EXTRACTED",
                        "IDENTIFIER_VALIDATED",
                        "MATCH_PROPOSED");
    }

    @Test
    void should_auto_allocate_when_structured_matching_resolves_single_eligible_debt() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        AllocationProposalCandidateRepository candidateRepository = mock(AllocationProposalCandidateRepository.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        AllocationTransactionalWorker allocationTransactionalWorker = mock(AllocationTransactionalWorker.class);

        PaymentMatchingApplicationService service = new PaymentMatchingApplicationService(
                paymentRepository,
                debtorRepository,
                debtRepository,
                proposalRepository,
                candidateRepository,
                auditEventGateway,
                allocationTransactionalWorker);

        Instant now = Instant.parse("2026-03-10T10:00:00Z");
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.received(
                paymentId,
                "TX-MATCH-2",
                new BigDecimal("80.00"),
                "EUR",
                "+++123/4567/89002+++",
                null,
                null,
                null,
                now);
        Debt debt = Debt.open(UUID.randomUUID(), UUID.randomUUID(), "123456789002", new BigDecimal("30.00"), "EUR", null, now);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(debtRepository.findByReference(any())).thenReturn(List.of(debt));

        MatchPaymentResult result = service.matchPayment(new MatchPaymentCommand(paymentId, MatchingMethod.STRUCTURED_COMMUNICATION, now));

        assertThat(result.matchingMethod()).isEqualTo(MatchingMethod.STRUCTURED_COMMUNICATION);
        assertThat(result.reason()).contains("STRUCTURED_COMMUNICATION_UNIQUE_ELIGIBLE_DEBT");
        assertThat(result.autoAllocationExecuted()).isTrue();
        assertThat(result.proposalId()).isEmpty();

        ArgumentCaptor<AllocationExecutionRequest> requestCaptor = ArgumentCaptor.forClass(AllocationExecutionRequest.class);
        verify(allocationTransactionalWorker).executeAllocation(requestCaptor.capture());
        assertThat(requestCaptor.getValue().paymentId()).isEqualTo(paymentId);
        assertThat(requestCaptor.getValue().debtId()).isEqualTo(debt.id());
        assertThat(requestCaptor.getValue().proposalId()).isNull();
        verify(candidateRepository, never()).save(any());
        verify(proposalRepository, never()).save(any());
        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventGateway, times(2)).append(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues().stream().map(AuditEvent::eventType).toList())
                .contains("STRUCTURED_COMMUNICATION_NORMALIZED", "STRUCTURED_COMMUNICATION_VALIDATED");
    }

    @Test
    void should_fallback_to_name_path_reason_when_no_identifier_candidate_exists() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        AllocationProposalCandidateRepository candidateRepository = mock(AllocationProposalCandidateRepository.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        AllocationTransactionalWorker allocationTransactionalWorker = mock(AllocationTransactionalWorker.class);

        PaymentMatchingApplicationService service = new PaymentMatchingApplicationService(
                paymentRepository,
                debtorRepository,
                debtRepository,
                proposalRepository,
                candidateRepository,
                auditEventGateway,
                allocationTransactionalWorker);

        Instant now = Instant.parse("2026-03-10T11:00:00Z");
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.received(paymentId, "TX-MATCH-3", new BigDecimal("80.00"), "EUR", null, "no id", "name", null, now);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(proposalRepository.save(any(AllocationProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(debtorRepository.findAllActive()).thenReturn(List.of());

        MatchPaymentResult result = service.matchPayment(new MatchPaymentCommand(paymentId, MatchingMethod.NAME, now));

        assertThat(result.matchingMethod()).isEqualTo(MatchingMethod.NAME);
        assertThat(result.reason()).contains("NAME_MATCH_NO_ACTIVE_DEBTOR");
        assertThat(result.autoAllocationExecuted()).isFalse();
        assertThat(result.proposalId()).isPresent();
        assertThat(payment.status()).isEqualTo(PaymentStatus.TO_MATCH);

        ArgumentCaptor<AllocationProposal> proposalCaptor = ArgumentCaptor.forClass(AllocationProposal.class);
        verify(proposalRepository).save(proposalCaptor.capture());
        assertThat(proposalCaptor.getValue().matchingMethod()).isEqualTo(MatchingMethod.NAME);
    }

    @Test
    void should_resolve_identifier_candidates_from_valid_national_register_number() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        AllocationProposalCandidateRepository candidateRepository = mock(AllocationProposalCandidateRepository.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        AllocationTransactionalWorker allocationTransactionalWorker = mock(AllocationTransactionalWorker.class);

        PaymentMatchingApplicationService service = new PaymentMatchingApplicationService(
                paymentRepository,
                debtorRepository,
                debtRepository,
                proposalRepository,
                candidateRepository,
                auditEventGateway,
                allocationTransactionalWorker);

        Instant now = Instant.parse("2026-03-10T12:00:00Z");
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.received(
                paymentId,
                "TX-MATCH-4",
                new BigDecimal("200.00"),
                "EUR",
                null,
                "payer niss 85073003328",
                "John Doe",
                null,
                now);
        Debtor debtor = Debtor.activeNaturalPerson(UUID.randomUUID(), "John Doe", "85073003328", now);
        Debt debt = Debt.open(UUID.randomUUID(), debtor.id(), "DEBT-NISS-1", new BigDecimal("60.00"), "EUR", null, now);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(debtorRepository.findByNationalNumber("85073003328")).thenReturn(Optional.of(debtor));
        when(debtRepository.findByDebtorIds(anySet())).thenReturn(List.of(debt));
        when(proposalRepository.save(any(AllocationProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(candidateRepository.save(any(AllocationProposalCandidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchPaymentResult result = service.matchPayment(new MatchPaymentCommand(paymentId, MatchingMethod.IDENTIFIER, now));

        assertThat(result.matchingMethod()).isEqualTo(MatchingMethod.IDENTIFIER);
        assertThat(result.reason()).contains("IDENTIFIER_MATCH_SUCCESS");
        assertThat(result.proposalId()).isPresent();
        assertThat(payment.status()).isEqualTo(PaymentStatus.MATCH_PROPOSED);
        verify(candidateRepository).save(any(AllocationProposalCandidate.class));
    }

    @Test
    void should_attempt_real_name_scoring_when_name_path_is_reached() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        AllocationProposalCandidateRepository candidateRepository = mock(AllocationProposalCandidateRepository.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        NameMatchScorer nameMatchScorer = mock(NameMatchScorer.class);
        AllocationTransactionalWorker allocationTransactionalWorker = mock(AllocationTransactionalWorker.class);

        PaymentMatchingApplicationService service = new PaymentMatchingApplicationService(
                paymentRepository,
                debtorRepository,
                debtRepository,
                proposalRepository,
                candidateRepository,
                auditEventGateway,
                allocationTransactionalWorker,
                nameMatchScorer);

        Instant now = Instant.parse("2026-03-10T13:00:00Z");
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.received(
                paymentId,
                "TX-MATCH-5",
                new BigDecimal("80.00"),
                "EUR",
                null,
                "BE0820501224",
                "Acme Belgium",
                null,
                now);
        Debtor debtor = Debtor.activeEnterprise(UUID.randomUUID(), "Acme Belgium", "0820501224", now);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(debtorRepository.findAllActive()).thenReturn(List.of(debtor));
        when(debtRepository.findByDebtorIds(anySet())).thenReturn(List.of());
        when(proposalRepository.save(any(AllocationProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(nameMatchScorer.score("Acme Belgium", "Acme Belgium")).thenReturn(MatchConfidence.HIGH);

        MatchPaymentResult result = service.matchPayment(new MatchPaymentCommand(paymentId, MatchingMethod.NAME, now));

        assertThat(result.matchingMethod()).isEqualTo(MatchingMethod.NAME);
        assertThat(result.reason()).contains("NAME_MATCH_NO_ELIGIBLE_CONFIDENT_CANDIDATE");
        verify(nameMatchScorer, times(1)).score("Acme Belgium", "Acme Belgium");
    }
}
