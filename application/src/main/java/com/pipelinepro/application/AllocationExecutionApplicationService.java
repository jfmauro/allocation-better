package com.pipelinepro.application;

import com.pipelinepro.application.error.ApplicationConflictException;
import com.pipelinepro.application.error.ResourceNotFoundException;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.in.ExecuteAllocationUseCase;
import com.pipelinepro.domain.port.in.command.ExecuteAllocationCommand;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import com.pipelinepro.domain.port.out.AllocationTransactionalWorker;
import com.pipelinepro.domain.port.out.DebtRepository;
import com.pipelinepro.domain.port.out.PaymentRepository;
import com.pipelinepro.domain.port.out.command.AllocationExecutionRequest;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AllocationExecutionApplicationService implements ExecuteAllocationUseCase {

    private static final Logger log = LoggerFactory.getLogger(AllocationExecutionApplicationService.class);

    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;
    private final AllocationProposalRepository allocationProposalRepository;
    private final AllocationTransactionalWorker allocationTransactionalWorker;

    public AllocationExecutionApplicationService(
            PaymentRepository paymentRepository,
            DebtRepository debtRepository,
            AllocationProposalRepository allocationProposalRepository,
            AllocationTransactionalWorker allocationTransactionalWorker) {
        this.paymentRepository = paymentRepository;
        this.debtRepository = debtRepository;
        this.allocationProposalRepository = allocationProposalRepository;
        this.allocationTransactionalWorker = allocationTransactionalWorker;
    }

    @Override
    public PaymentAllocation executeAllocation(ExecuteAllocationCommand command) {
        log.info("+++start executeAllocation+++");
        try {
            assertReferencesExist(command);
            AllocationExecutionRequest request = new AllocationExecutionRequest(
                    command.paymentId(),
                    command.debtId(),
                    command.proposalId(),
                    command.amount(),
                    command.idempotencyKey(),
                    command.commandId(),
                    command.actor(),
                    command.occurredAt());
            return allocationTransactionalWorker.executeAllocation(request);
        } finally {
            log.info("+++end executeAllocation+++");
        }
    }

    private void assertReferencesExist(ExecuteAllocationCommand command) {
        if (paymentRepository.findById(command.paymentId()).isEmpty()) {
            throw new ResourceNotFoundException("Payment not found");
        }
        if (debtRepository.findById(command.debtId()).isEmpty()) {
            throw new ResourceNotFoundException("Debt not found");
        }
        Optional<AllocationProposal> proposal = command.proposalId() == null
                ? Optional.empty()
                : allocationProposalRepository.findById(command.proposalId());
        if (command.proposalId() != null && proposal.isEmpty()) {
            throw new ResourceNotFoundException("Allocation proposal not found");
        }
        proposal.ifPresent(existingProposal -> assertProposalConsistency(command, existingProposal));
    }

    private void assertProposalConsistency(ExecuteAllocationCommand command, AllocationProposal proposal) {
        if (!proposal.paymentId().equals(command.paymentId())) {
            throw new ApplicationConflictException("Proposal payment mismatch");
        }
        proposal.selectedDebtId()
                .filter(selectedDebtId -> !selectedDebtId.equals(command.debtId()))
                .ifPresent(selectedDebtId -> {
                    throw new ApplicationConflictException("Proposal debt mismatch");
                });
    }
}
