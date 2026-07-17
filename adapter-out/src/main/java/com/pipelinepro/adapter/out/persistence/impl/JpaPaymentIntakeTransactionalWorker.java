package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.SourceAggregateType;
import com.pipelinepro.domain.port.in.MatchPaymentUseCase;
import com.pipelinepro.domain.port.in.command.MatchPaymentCommand;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import com.pipelinepro.domain.port.out.AuditEventGateway;
import com.pipelinepro.domain.port.out.AccountingEntryRepository;
import com.pipelinepro.domain.port.out.PaymentRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class JpaPaymentIntakeTransactionalWorker {

    private static final Logger log = LoggerFactory.getLogger(JpaPaymentIntakeTransactionalWorker.class);

    private final PaymentRepository paymentRepository;
    private final AuditEventGateway auditEventGateway;
    private final MatchPaymentUseCase matchPaymentUseCase;
    private final AccountingEntryRepository accountingEntryRepository;
    private final TransactionTemplate matchingTransactionTemplate;

    public JpaPaymentIntakeTransactionalWorker(
            PaymentRepository paymentRepository,
            AuditEventGateway auditEventGateway,
            MatchPaymentUseCase matchPaymentUseCase,
            AccountingEntryRepository accountingEntryRepository,
            PlatformTransactionManager transactionManager) {
        this.paymentRepository = paymentRepository;
        this.auditEventGateway = auditEventGateway;
        this.matchPaymentUseCase = matchPaymentUseCase;
        this.accountingEntryRepository = accountingEntryRepository;
        this.matchingTransactionTemplate = new TransactionTemplate(transactionManager);
        this.matchingTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Payment receivePayment(ReceivePaymentCommand command) {
        log.info("+++start receivePayment+++");
        try {
            validateDatePolicy(command);
            paymentRepository.findByBankTransactionReference(command.bankTransactionReference())
                    .ifPresent(existing -> {
                        throw duplicatePaymentConflict(command.bankTransactionReference());
                    });

            Payment receivedPayment = Payment.received(
                    command.paymentId(),
                    command.bankTransactionReference(),
                    command.amount(),
                    command.currency(),
                    command.structuredCommunication(),
                    command.freeCommunication(),
                    command.payerName(),
                    command.payerIbanMasked(),
                    command.receivedAt());

            Payment persistedPayment;
            try {
                persistedPayment = paymentRepository.save(receivedPayment);
            } catch (DataIntegrityViolationException exception) {
                throw duplicatePaymentConflict(command.bankTransactionReference(), exception);
            }
            accountingEntryRepository.append(AccountingEntry.append(
                    UUID.randomUUID(),
                    AccountingEventType.PAYMENT_ARRIVAL,
                    SourceAggregateType.PAYMENT,
                    persistedPayment.id(),
                    persistedPayment.amount(),
                    persistedPayment.currency(),
                    command.valueDate(),
                    command.receivedAt()));
            auditEventGateway.append(buildPaymentReceivedAuditEvent(persistedPayment, command));
            scheduleMatchingAfterCommit(persistedPayment, command);
            return persistedPayment;
        } finally {
            log.info("+++end receivePayment+++");
        }
    }

    private void validateDatePolicy(ReceivePaymentCommand command) {
        if (command.valueDate().isAfter(command.receivedAt())) {
            throw new IllegalArgumentException("valueDate must not be after receivedAt");
        }
    }

    private IllegalStateException duplicatePaymentConflict(
            String bankTransactionReference,
            DataIntegrityViolationException exception) {
        return new IllegalStateException(
                "Payment already exists for bankTransactionReference: " + bankTransactionReference,
                exception);
    }

    private IllegalStateException duplicatePaymentConflict(String bankTransactionReference) {
        return new IllegalStateException(
                "Payment already exists for bankTransactionReference: " + bankTransactionReference);
    }

    private void scheduleMatchingAfterCommit(Payment payment, ReceivePaymentCommand command) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            // Keep payment and accounting atomic while matching observes only committed intake state.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runMatchingInIsolatedTransaction(payment, command);
                }
            });
            return;
        }
        runMatchingInIsolatedTransaction(payment, command);
    }

    private void runMatchingInIsolatedTransaction(Payment payment, ReceivePaymentCommand command) {
        try {
            matchingTransactionTemplate.executeWithoutResult(status ->
                    matchPaymentUseCase.matchPayment(new MatchPaymentCommand(
                            payment.id(),
                            MatchingMethod.NAME,
                            command.receivedAt())));
        } catch (RuntimeException exception) {
            log.error("+++post-commit payment matching failed; matching can be retried+++", exception);
        }
    }

    private AuditEvent buildPaymentReceivedAuditEvent(Payment payment, ReceivePaymentCommand command) {
        String payloadJson = "{\"bankTransactionReference\":\""
                + escapeJson(command.bankTransactionReference())
                + "\",\"amount\":\""
                + command.amount().toPlainString()
                + "\",\"currency\":\""
                + escapeJson(command.currency())
                + "\"}";
        return new AuditEvent(
                UUID.randomUUID(),
                "PAYMENT",
                payment.id(),
                "PAYMENT_RECEIVED",
                null,
                payloadJson,
                command.receivedAt());
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
