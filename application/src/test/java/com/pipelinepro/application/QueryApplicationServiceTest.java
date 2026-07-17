package com.pipelinepro.application;

import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.DebtorType;
import com.pipelinepro.domain.port.out.DebtorRepository;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import com.pipelinepro.domain.port.out.DebtRepository;
import com.pipelinepro.domain.port.out.PaymentAllocationRepository;
import com.pipelinepro.domain.port.out.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QueryApplicationServiceTest {

    @Test
    void should_filter_debts_by_status_when_status_filter_provided() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentAllocationRepository paymentAllocationRepository = mock(PaymentAllocationRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        QueryApplicationService service = new QueryApplicationService(
                paymentRepository,
                proposalRepository,
                paymentAllocationRepository,
                debtRepository,
                debtorRepository);

        UUID debtorId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-01T10:00:00Z");
        Debt open = Debt.open(UUID.randomUUID(), debtorId, "D-OPEN", new BigDecimal("100.00"), "EUR", null, now);
        Debt paid = Debt.open(UUID.randomUUID(), debtorId, "D-PAID", new BigDecimal("50.00"), "EUR", null, now);
        paid.pay(new BigDecimal("50.00"), now.plusSeconds(5));

        when(debtRepository.findByDebtorId(debtorId)).thenReturn(List.of(open, paid));

        List<Debt> debts = service.listDebtsByDebtor(debtorId, List.of(DebtStatus.OPEN));

        assertThat(debts).containsExactly(open);
    }

    @Test
    void should_delegate_read_queries_when_requesting_read_models() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentAllocationRepository paymentAllocationRepository = mock(PaymentAllocationRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        QueryApplicationService service = new QueryApplicationService(
                paymentRepository,
                proposalRepository,
                paymentAllocationRepository,
                debtRepository,
                debtorRepository);

        UUID paymentId = UUID.randomUUID();
        UUID proposalId = UUID.randomUUID();
        UUID allocationId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.empty());
        when(paymentAllocationRepository.findById(allocationId)).thenReturn(Optional.empty());
        when(debtRepository.findById(debtId)).thenReturn(Optional.empty());

        service.getPayment(paymentId);
        service.getProposal(proposalId);
        service.getAllocation(allocationId);
        service.getDebt(debtId);
        service.listProposals(paymentId);

        verify(paymentRepository).findById(paymentId);
        verify(proposalRepository).findById(proposalId);
        verify(paymentAllocationRepository).findById(allocationId);
        verify(debtRepository).findById(debtId);
        verify(proposalRepository).findByPaymentId(paymentId);
    }

    @Test
    void should_delegate_batch_queries_for_debts_and_debtors() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentAllocationRepository paymentAllocationRepository = mock(PaymentAllocationRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        QueryApplicationService service = new QueryApplicationService(
                paymentRepository,
                proposalRepository,
                paymentAllocationRepository,
                debtRepository,
                debtorRepository);

        UUID debtId = UUID.randomUUID();
        UUID debtorId = UUID.randomUUID();
        Debt debt = Debt.open(debtId, debtorId, "D-BATCH", new BigDecimal("10.00"), "EUR", null, Instant.parse("2026-06-01T13:00:00Z"));
        Debtor debtor = Debtor.activeNaturalPerson(debtorId, "Batch Debtor", "85073003328", Instant.parse("2026-06-01T13:00:00Z"));
        when(debtRepository.findByIds(Set.of(debtId))).thenReturn(List.of(debt));
        when(debtorRepository.findByIds(Set.of(debtorId))).thenReturn(List.of(debtor));

        assertThat(service.getDebts(Set.of(debtId))).containsExactly(debt);
        assertThat(service.listDebtors(Set.of(debtorId))).containsExactly(debtor);

        verify(debtRepository).findByIds(Set.of(debtId));
        verify(debtorRepository).findByIds(Set.of(debtorId));
    }

    @Test
    void should_default_to_allocatable_statuses_when_status_filter_is_empty() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentAllocationRepository paymentAllocationRepository = mock(PaymentAllocationRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        QueryApplicationService service = new QueryApplicationService(
                paymentRepository,
                proposalRepository,
                paymentAllocationRepository,
                debtRepository,
                debtorRepository);

        UUID debtorId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-01T11:00:00Z");
        Debt open = Debt.open(UUID.randomUUID(), debtorId, "D-OPEN", new BigDecimal("100.00"), "EUR", null, now);
        Debt paid = Debt.open(UUID.randomUUID(), debtorId, "D-PAID", new BigDecimal("50.00"), "EUR", null, now);
        paid.pay(new BigDecimal("50.00"), now.plusSeconds(5));

        when(debtRepository.findByDebtorId(debtorId)).thenReturn(List.of(open, paid));

        List<Debt> debts = service.listDebtsByDebtor(debtorId, List.of());

        assertThat(debts).containsExactly(open);
    }

    @Test
    void should_default_to_allocatable_statuses_when_status_filter_is_null() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentAllocationRepository paymentAllocationRepository = mock(PaymentAllocationRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        QueryApplicationService service = new QueryApplicationService(
                paymentRepository,
                proposalRepository,
                paymentAllocationRepository,
                debtRepository,
                debtorRepository);

        UUID debtorId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-01T12:00:00Z");
        Debt open = Debt.open(UUID.randomUUID(), debtorId, "D-OPEN", new BigDecimal("100.00"), "EUR", null, now);
        Debt paid = Debt.open(UUID.randomUUID(), debtorId, "D-PAID", new BigDecimal("50.00"), "EUR", null, now);
        paid.pay(new BigDecimal("50.00"), now.plusSeconds(5));

        when(debtRepository.findByDebtorId(debtorId)).thenReturn(List.of(open, paid));

        List<Debt> debts = service.listDebtsByDebtor(debtorId, null);

        assertThat(debts).containsExactly(open);
    }

    @Test
    void should_filter_debtors_by_query_type_and_active_flag() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        PaymentAllocationRepository paymentAllocationRepository = mock(PaymentAllocationRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        QueryApplicationService service = new QueryApplicationService(
                paymentRepository,
                proposalRepository,
                paymentAllocationRepository,
                debtRepository,
                debtorRepository);

        Debtor alice = Debtor.activeNaturalPerson(UUID.randomUUID(), "Alice Example", "85073003328", Instant.now());
        Debtor acme = Debtor.activeEnterprise(UUID.randomUUID(), "Acme Corp", "0820501224", Instant.now());
        when(debtorRepository.findAllActive()).thenReturn(List.of(alice, acme));

        List<Debtor> results = service.listDebtors(new com.pipelinepro.domain.port.in.command.DebtorSearchCriteria("alice", DebtorType.NATURAL_PERSON, true));

        assertThat(results).containsExactly(alice);
    }
}
