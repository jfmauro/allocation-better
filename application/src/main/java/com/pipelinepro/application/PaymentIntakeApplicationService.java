package com.pipelinepro.application;

import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.in.MatchPaymentUseCase;
import com.pipelinepro.domain.port.in.ReceivePaymentUseCase;
import com.pipelinepro.domain.port.in.command.MatchPaymentCommand;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import com.pipelinepro.domain.port.out.AuditEventGateway;
import com.pipelinepro.domain.port.out.PaymentRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PaymentIntakeApplicationService implements ReceivePaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentIntakeApplicationService.class);

    private final PaymentRepository paymentRepository;
    private final AuditEventGateway auditEventGateway;
    private final MatchPaymentUseCase matchPaymentUseCase;

    public PaymentIntakeApplicationService(
            PaymentRepository paymentRepository,
            AuditEventGateway auditEventGateway,
            MatchPaymentUseCase matchPaymentUseCase) {
        this.paymentRepository = paymentRepository;
        this.auditEventGateway = auditEventGateway;
        this.matchPaymentUseCase = matchPaymentUseCase;
    }

    @Override
    public Payment receivePayment(ReceivePaymentCommand command) {
        log.info("+++start receivePayment+++");
        try {
            paymentRepository.findByBankTransactionReference(command.bankTransactionReference())
                    .ifPresent(existing -> {
                        throw new IllegalStateException("Payment already exists for bankTransactionReference: "
                                + command.bankTransactionReference());
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

            Payment persistedPayment = paymentRepository.save(receivedPayment);
            auditEventGateway.append(buildPaymentReceivedAuditEvent(persistedPayment, command));
            matchPaymentUseCase.matchPayment(new MatchPaymentCommand(
                    persistedPayment.id(),
                    MatchingMethod.NAME,
                    command.receivedAt()));
            return persistedPayment;
        } finally {
            log.info("+++end receivePayment+++");
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
