package com.pipelinepro.application;

import com.pipelinepro.application.error.ApplicationConflictException;
import com.pipelinepro.application.error.ResourceNotFoundException;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.ProposalStatus;
import com.pipelinepro.domain.port.in.ProposalLifecycleUseCase;
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
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProposalLifecycleApplicationService implements ProposalLifecycleUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProposalLifecycleApplicationService.class);

    private final AllocationProposalRepository allocationProposalRepository;
    private final PaymentRepository paymentRepository;
    private final AllocationTransactionalWorker allocationTransactionalWorker;
    private final AuditEventGateway auditEventGateway;

    public ProposalLifecycleApplicationService(
            AllocationProposalRepository allocationProposalRepository,
            PaymentRepository paymentRepository,
            AllocationTransactionalWorker allocationTransactionalWorker,
            AuditEventGateway auditEventGateway) {
        this.allocationProposalRepository = allocationProposalRepository;
        this.paymentRepository = paymentRepository;
        this.allocationTransactionalWorker = allocationTransactionalWorker;
        this.auditEventGateway = auditEventGateway;
    }

    @Override
    public PaymentAllocation validateProposal(ValidateProposalCommand command) {
        log.info("+++start validateProposal+++");
        try {
            AllocationProposal proposal = loadProposal(command.proposalId());
            AllocationProposal persistedProposal = prepareProposalForValidation(command, proposal);

            AllocationExecutionRequest request = new AllocationExecutionRequest(
                    persistedProposal.paymentId(),
                    command.debtId(),
                    persistedProposal.id(),
                    command.amount(),
                    validationIdempotencyKey(command),
                    validationCommandId(command),
                    command.actor(),
                    command.occurredAt());
            return allocationTransactionalWorker.executeAllocation(request);
        } finally {
            log.info("+++end validateProposal+++");
        }
    }

    private AllocationProposal prepareProposalForValidation(
            ValidateProposalCommand command,
            AllocationProposal proposal) {
        if (proposal.status() == ProposalStatus.PROPOSED) {
            proposal.selectDebt(command.actor(), command.debtId(), command.occurredAt());
            proposal.validate(command.actor(), command.occurredAt());
            return allocationProposalRepository.save(proposal);
        }

        if (proposal.status() == ProposalStatus.VALIDATED) {
            boolean sameDebt = proposal.selectedDebtId().isPresent()
                    && proposal.selectedDebtId().orElseThrow().equals(command.debtId());
            if (!sameDebt) {
                throw new ApplicationConflictException("Validated proposal already targets another debt");
            }
            return proposal;
        }

        throw new ApplicationConflictException("Only PROPOSED or VALIDATED proposals can be allocated");
    }

    @Override
    public AllocationProposal rejectProposal(RejectProposalCommand command) {
        log.info("+++start rejectProposal+++");
        try {
            AllocationProposal proposal = loadProposal(command.proposalId());
            proposal.reject(command.actor(), command.reason(), command.occurredAt());
            AllocationProposal persistedProposal = allocationProposalRepository.save(proposal);
            appendProposalLifecycleAuditEvent(
                    persistedProposal,
                    "USER_REJECTED_PROPOSAL",
                    command.actor(),
                    command.reason(),
                    command.occurredAt());
            return persistedProposal;
        } finally {
            log.info("+++end rejectProposal+++");
        }
    }

    @Override
    public AllocationProposal selectDebt(SelectDebtCommand command) {
        log.info("+++start selectDebt+++");
        try {
            AllocationProposal proposal = loadProposal(command.proposalId());
            proposal.selectDebt(command.actor(), command.debtId(), command.occurredAt());
            return allocationProposalRepository.save(proposal);
        } finally {
            log.info("+++end selectDebt+++");
        }
    }

    @Override
    public AllocationProposal markUnmatched(MarkUnmatchedCommand command) {
        log.info("+++start markUnmatched+++");
        try {
            AllocationProposal proposal = loadProposal(command.proposalId());
            proposal.markUnmatched(command.actor(), command.reason(), command.occurredAt());
            AllocationProposal persistedProposal = allocationProposalRepository.save(proposal);
            markPaymentUnmatched(persistedProposal.paymentId(), command.occurredAt());
            appendProposalLifecycleAuditEvent(
                    persistedProposal,
                    "PAYMENT_MARKED_UNMATCHED",
                    command.actor(),
                    command.reason(),
                    command.occurredAt());
            return persistedProposal;
        } finally {
            log.info("+++end markUnmatched+++");
        }
    }

    @Override
    public AllocationProposal requestInvestigation(RequestInvestigationCommand command) {
        log.info("+++start requestInvestigation+++");
        try {
            AllocationProposal proposal = loadProposal(command.proposalId());
            proposal.requestInvestigation(command.actor(), command.reason(), command.occurredAt());
            AllocationProposal persistedProposal = allocationProposalRepository.save(proposal);
            markPaymentForInvestigation(persistedProposal.paymentId(), command.occurredAt());
            appendProposalLifecycleAuditEvent(
                    persistedProposal,
                    "PAYMENT_SENT_TO_INVESTIGATION",
                    command.actor(),
                    command.reason(),
                    command.occurredAt());
            return persistedProposal;
        } finally {
            log.info("+++end requestInvestigation+++");
        }
    }

    private AllocationProposal loadProposal(java.util.UUID proposalId) {
        return allocationProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation proposal not found"));
    }

    private void markPaymentUnmatched(java.util.UUID paymentId, java.time.Instant occurredAt) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.markUnmatched(occurredAt);
        paymentRepository.save(payment);
    }

    private void markPaymentForInvestigation(java.util.UUID paymentId, java.time.Instant occurredAt) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.requestInvestigation(occurredAt);
        paymentRepository.save(payment);
    }

    private String validationIdempotencyKey(ValidateProposalCommand command) {
        return "proposal-validation:"
                + command.proposalId()
                + ":"
                + command.debtId()
                + ":"
                + reasonToken(command.reason());
    }

    private String validationCommandId(ValidateProposalCommand command) {
        return "validate-proposal:" + command.proposalId() + ":" + reasonToken(command.reason());
    }

    private String reasonToken(String reason) {
        return reason
                .trim()
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private void appendProposalLifecycleAuditEvent(
            AllocationProposal proposal,
            String eventType,
            String actor,
            String reason,
            java.time.Instant occurredAt) {
        String payloadJson = "{\"proposalId\":\""
                + proposal.id()
                + "\",\"paymentId\":\""
                + proposal.paymentId()
                + "\",\"reason\":\""
                + escapeJson(reason)
                + "\"}";
        auditEventGateway.append(new AuditEvent(
                UUID.randomUUID(),
                "ALLOCATION_PROPOSAL",
                proposal.id(),
                eventType,
                actor,
                payloadJson,
                occurredAt));
    }

    private String escapeJson(String value) {
        return (value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
