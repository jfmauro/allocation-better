package com.pipelinepro.application;

import com.pipelinepro.application.error.ResourceNotFoundException;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.port.in.command.ExecuteAllocationCommand;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import com.pipelinepro.domain.port.out.AllocationTransactionalWorker;
import com.pipelinepro.domain.port.out.DebtRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AllocationExecutionApplicationServiceTest {

    @Test
    void should_delegate_effective_allocation_to_transactional_worker() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        AllocationProposalRepository allocationProposalRepository = mock(AllocationProposalRepository.class);
        AllocationTransactionalWorker worker = mock(AllocationTransactionalWorker.class);
        AllocationExecutionApplicationService service = new AllocationExecutionApplicationService(
                paymentRepository,
                debtRepository,
                allocationProposalRepository,
                worker);

        Instant now = Instant.parse("2026-05-01T09:00:00Z");
        UUID paymentId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();
        UUID proposalId = UUID.randomUUID();

        ExecuteAllocationCommand command = new ExecuteAllocationCommand(
                paymentId,
                debtId,
                proposalId,
                new BigDecimal("70.00"),
                "idem-1",
                "cmd-1",
                "allocator",
                now);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(mock(com.pipelinepro.domain.Payment.class)));
        when(debtRepository.findById(debtId)).thenReturn(Optional.of(mock(com.pipelinepro.domain.Debt.class)));
        when(allocationProposalRepository.findById(proposalId)).thenReturn(Optional.of(
                AllocationProposal.proposed(
                        proposalId,
                        paymentId,
                        MatchingMethod.NAME,
                        "seed",
                        now)));

        service.executeAllocation(command);

        ArgumentCaptor<AllocationExecutionRequest> requestCaptor = ArgumentCaptor.forClass(AllocationExecutionRequest.class);
        verify(worker).executeAllocation(requestCaptor.capture());

        AllocationExecutionRequest request = requestCaptor.getValue();
        assertThat(request.paymentId()).isEqualTo(paymentId);
        assertThat(request.debtId()).isEqualTo(debtId);
        assertThat(request.proposalId()).isEqualTo(proposalId);
        assertThat(request.amount()).isEqualByComparingTo("70.00");
        assertThat(request.idempotencyKey()).isEqualTo("idem-1");
        assertThat(request.commandId()).isEqualTo("cmd-1");
        assertThat(request.actor()).isEqualTo("allocator");
        assertThat(request.occurredAt()).isEqualTo(now);
    }

    @Test
    void should_throw_not_found_when_payment_is_missing() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        AllocationProposalRepository allocationProposalRepository = mock(AllocationProposalRepository.class);
        AllocationTransactionalWorker worker = mock(AllocationTransactionalWorker.class);
        AllocationExecutionApplicationService service = new AllocationExecutionApplicationService(
                paymentRepository,
                debtRepository,
                allocationProposalRepository,
                worker);

        ExecuteAllocationCommand command = new ExecuteAllocationCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new BigDecimal("10.00"),
                "idem-1",
                "cmd-1",
                "allocator",
                Instant.parse("2026-05-01T09:00:00Z"));
        when(paymentRepository.findById(command.paymentId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executeAllocation(command)).isInstanceOf(ResourceNotFoundException.class);
        verify(worker, never()).executeAllocation(any());
    }
}
