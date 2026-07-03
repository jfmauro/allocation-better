package com.pipelinepro.adapter.out.persistence.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.pipelinepro.adapter.out.persistence.entity.AllocationProposalEntity;
import com.pipelinepro.adapter.out.persistence.entity.DebtEntity;
import com.pipelinepro.adapter.out.persistence.entity.PaymentEntity;
import com.pipelinepro.adapter.out.persistence.mapper.AuditEventEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.AllocationProposalEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.PaymentAllocationEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.PaymentEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentAllocationRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.ProposalStatus;
import com.pipelinepro.domain.port.out.AllocationTransactionalWorker;
import com.pipelinepro.domain.port.out.command.AllocationExecutionRequest;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaAllocationTransactionalWorker implements AllocationTransactionalWorker {

    private static final Logger log = LoggerFactory.getLogger(JpaAllocationTransactionalWorker.class);

    private final SpringDataPaymentRepository springDataPaymentRepository;
    private final SpringDataDebtRepository springDataDebtRepository;
    private final SpringDataAllocationProposalRepository springDataAllocationProposalRepository;
    private final SpringDataPaymentAllocationRepository springDataPaymentAllocationRepository;
    private final SpringDataAuditEventRepository springDataAuditEventRepository;
    private final PaymentEntityMapper paymentEntityMapper;
    private final DebtEntityMapper debtEntityMapper;
    private final PaymentAllocationEntityMapper paymentAllocationEntityMapper;
    private final AuditEventEntityMapper auditEventEntityMapper;
    private final AllocationProposalEntityMapper allocationProposalEntityMapper;
    private final ObjectMapper objectMapper;

    public JpaAllocationTransactionalWorker(
            SpringDataPaymentRepository springDataPaymentRepository,
            SpringDataDebtRepository springDataDebtRepository,
            SpringDataAllocationProposalRepository springDataAllocationProposalRepository,
            SpringDataPaymentAllocationRepository springDataPaymentAllocationRepository,
            SpringDataAuditEventRepository springDataAuditEventRepository,
            PaymentEntityMapper paymentEntityMapper,
            DebtEntityMapper debtEntityMapper,
            PaymentAllocationEntityMapper paymentAllocationEntityMapper,
            AuditEventEntityMapper auditEventEntityMapper,
            AllocationProposalEntityMapper allocationProposalEntityMapper) {
        this.springDataPaymentRepository = springDataPaymentRepository;
        this.springDataDebtRepository = springDataDebtRepository;
        this.springDataAllocationProposalRepository = springDataAllocationProposalRepository;
        this.springDataPaymentAllocationRepository = springDataPaymentAllocationRepository;
        this.springDataAuditEventRepository = springDataAuditEventRepository;
        this.paymentEntityMapper = paymentEntityMapper;
        this.debtEntityMapper = debtEntityMapper;
        this.paymentAllocationEntityMapper = paymentAllocationEntityMapper;
        this.auditEventEntityMapper = auditEventEntityMapper;
        this.allocationProposalEntityMapper = allocationProposalEntityMapper;
        this.objectMapper = JsonMapper.builder().build();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentAllocation executeAllocation(AllocationExecutionRequest request) {
        log.info("+++start executeAllocation+++");
        try {
            var existingAllocation = springDataPaymentAllocationRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existingAllocation.isPresent()) {
                return paymentAllocationEntityMapper.toDomain(existingAllocation.orElseThrow());
            }

            PaymentEntity lockedPayment = springDataPaymentRepository
                    .findByIdForUpdate(request.paymentId())
                    .orElseThrow(() -> new IllegalStateException("Payment not found: " + request.paymentId()));

            DebtEntity lockedDebt = springDataDebtRepository
                    .findByIdForUpdate(request.debtId())
                    .orElseThrow(() -> new IllegalStateException("Debt not found: " + request.debtId()));

            AllocationProposalEntity lockedProposal = lockProposalIfPresent(request);

            var existingAllocationAfterLock = springDataPaymentAllocationRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existingAllocationAfterLock.isPresent()) {
                return paymentAllocationEntityMapper.toDomain(existingAllocationAfterLock.orElseThrow());
            }

            Payment payment = paymentEntityMapper.toDomain(lockedPayment);
            Debt debt = debtEntityMapper.toDomain(lockedDebt);

            PaymentAllocation allocation = PaymentAllocation.execute(
                    UUID.randomUUID(),
                    payment,
                    debt,
                    request.proposalId(),
                    request.amount(),
                    request.idempotencyKey(),
                    request.commandId(),
                    request.actor(),
                    request.occurredAt());

            PaymentAllocation persistedAllocation;
            try {
                var allocationEntity = paymentAllocationEntityMapper.toEntity(allocation);
                allocationEntity.setId(null);
                persistedAllocation = paymentAllocationEntityMapper.toDomain(
                        springDataPaymentAllocationRepository.saveAndFlush(allocationEntity));
            } catch (DataIntegrityViolationException duplicateKeyException) {
                return springDataPaymentAllocationRepository
                        .findByIdempotencyKey(request.idempotencyKey())
                        .map(paymentAllocationEntityMapper::toDomain)
                        .orElseThrow(() -> duplicateKeyException);
            }

            applyPaymentState(lockedPayment, payment);
            applyDebtState(lockedDebt, debt);
            updateProposalStateIfPresent(lockedProposal, request);

            springDataPaymentRepository.save(lockedPayment);
            springDataDebtRepository.save(lockedDebt);
            if (lockedProposal != null) {
                springDataAllocationProposalRepository.save(lockedProposal);
            }

            if (request.proposalId() != null) {
                persistAuditEvent(buildUserValidatedAllocationAuditEvent(request, lockedProposal));
            }
            persistAuditEvent(buildPaymentAllocatedAuditEvent(persistedAllocation, request));

            return persistedAllocation;
        } finally {
            log.info("+++end executeAllocation+++");
        }
    }

    private AllocationProposalEntity lockProposalIfPresent(AllocationExecutionRequest request) {
        if (request.proposalId() == null) {
            return null;
        }
        return springDataAllocationProposalRepository
                .findByIdForUpdate(request.proposalId())
                .orElseThrow(() -> new IllegalStateException("Allocation proposal not found: " + request.proposalId()));
    }

    private void applyPaymentState(PaymentEntity paymentEntity, Payment payment) {
        paymentEntity.setRemainingAmount(payment.remainingAmount());
        paymentEntity.setStatus(payment.status());
        paymentEntity.setUpdatedAt(payment.updatedAt());
    }

    private void applyDebtState(DebtEntity debtEntity, Debt debt) {
        debtEntity.setRemainingAmount(debt.remainingAmount());
        debtEntity.setStatus(debt.status());
        debtEntity.setUpdatedAt(debt.updatedAt());
    }

    private void updateProposalStateIfPresent(
            AllocationProposalEntity lockedProposal,
            AllocationExecutionRequest request) {
        if (lockedProposal == null) {
            return;
        }
        AllocationProposal proposal = allocationProposalEntityMapper.toDomain(lockedProposal);

        if (proposal.status() == ProposalStatus.PROPOSED) {
            proposal.validate(request.actor(), request.occurredAt());
            lockedProposal.setStatus(proposal.status());
            lockedProposal.setValidatedBy(proposal.validatedBy().orElse(null));
            lockedProposal.setValidatedAt(proposal.validatedAt().orElse(null));
            lockedProposal.setUpdatedAt(proposal.updatedAt());
        }
    }

    private AuditEvent buildPaymentAllocatedAuditEvent(PaymentAllocation allocation, AllocationExecutionRequest request) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("paymentId", allocation.paymentId().toString());
        payload.put("debtId", allocation.debtId().toString());
        payload.put("allocationId", allocation.id().toString());
        payload.put("amount", toPlainString(allocation.amount()));
        payload.put("idempotencyKey", request.idempotencyKey());

        return new AuditEvent(
                UUID.randomUUID(),
                "PAYMENT_ALLOCATION",
                allocation.id(),
                "PAYMENT_ALLOCATED",
                request.actor(),
                toJson(payload),
                request.occurredAt());
    }

    private AuditEvent buildUserValidatedAllocationAuditEvent(
            AllocationExecutionRequest request,
            AllocationProposalEntity lockedProposal) {
        String validationReason = lockedProposal == null ? null : lockedProposal.getReason();
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("paymentId", request.paymentId().toString());
        payload.put("debtId", request.debtId().toString());
        payload.put("proposalId", request.proposalId().toString());
        payload.put("amount", toPlainString(request.amount()));
        payload.put("validationReason", validationReason);
        payload.put("idempotencyKey", request.idempotencyKey());

        return new AuditEvent(
                UUID.randomUUID(),
                "ALLOCATION_PROPOSAL",
                request.proposalId(),
                "USER_VALIDATED_ALLOCATION",
                request.actor(),
                toJson(payload),
                request.occurredAt());
    }

    private String toJson(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize audit payload", exception);
        }
    }

    private String toPlainString(BigDecimal amount) {
        return amount == null ? null : amount.toPlainString();
    }

    private void persistAuditEvent(AuditEvent auditEvent) {
        var auditEventEntity = auditEventEntityMapper.toEntity(auditEvent);
        auditEventEntity.setId(null);
        springDataAuditEventRepository.save(auditEventEntity);
    }
}
