package com.pipelinepro.application;

import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.in.MatchPaymentUseCase;
import com.pipelinepro.domain.port.in.command.MatchPaymentCommand;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import com.pipelinepro.domain.port.out.AuditEventGateway;
import com.pipelinepro.domain.port.out.PaymentRepository;
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

class PaymentIntakeApplicationServiceTest {

    @Test
    void should_reject_duplicate_bank_transaction_reference() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        MatchPaymentUseCase matchPaymentUseCase = mock(MatchPaymentUseCase.class);
        PaymentIntakeApplicationService service = new PaymentIntakeApplicationService(paymentRepository, auditEventGateway, matchPaymentUseCase);

        Instant now = Instant.parse("2026-01-01T10:15:30Z");
        ReceivePaymentCommand command = new ReceivePaymentCommand(
                UUID.randomUUID(),
                "TX-DUP-1",
                now,
                now,
                new BigDecimal("100.00"),
                "EUR",
                null,
                "free text",
                null,
                "Payer",
                "BE**1234",
                now);

        Payment existing = Payment.received(UUID.randomUUID(), "TX-DUP-1", new BigDecimal("50.00"), "EUR", null, null, null, null, now);
        when(paymentRepository.findByBankTransactionReference("TX-DUP-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.receivePayment(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Payment already exists");

        verify(paymentRepository, never()).save(any());
        verify(auditEventGateway, never()).append(any());
        verify(matchPaymentUseCase, never()).matchPayment(any());
    }

    @Test
    void should_persist_received_payment_and_append_audit_event() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        AuditEventGateway auditEventGateway = mock(AuditEventGateway.class);
        MatchPaymentUseCase matchPaymentUseCase = mock(MatchPaymentUseCase.class);
        PaymentIntakeApplicationService service = new PaymentIntakeApplicationService(paymentRepository, auditEventGateway, matchPaymentUseCase);

        Instant now = Instant.parse("2026-02-03T12:00:00Z");
        UUID paymentId = UUID.randomUUID();
        ReceivePaymentCommand command = new ReceivePaymentCommand(
                paymentId,
                "TX-OK-1",
                now,
                now,
                new BigDecimal("120.00"),
                "EUR",
                "+++123/4567/89012+++",
                "BE0820501224",
                null,
                "Acme",
                "BE**4321",
                now);

        when(paymentRepository.findByBankTransactionReference("TX-OK-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = service.receivePayment(command);

        assertThat(payment.id()).isEqualTo(paymentId);
        assertThat(payment.bankTransactionReference()).isEqualTo("TX-OK-1");
        assertThat(payment.amount()).isEqualByComparingTo("120.00");
        assertThat(payment.remainingAmount()).isEqualByComparingTo("120.00");

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventGateway).append(auditCaptor.capture());
        AuditEvent event = auditCaptor.getValue();

        assertThat(event.aggregateType()).isEqualTo("PAYMENT");
        assertThat(event.aggregateId()).isEqualTo(paymentId);
        assertThat(event.eventType()).isEqualTo("PAYMENT_RECEIVED");
        assertThat(event.createdAt()).isEqualTo(now);
        assertThat(event.payloadJson()).contains("TX-OK-1");

        ArgumentCaptor<MatchPaymentCommand> matchCaptor = ArgumentCaptor.forClass(MatchPaymentCommand.class);
        verify(matchPaymentUseCase).matchPayment(matchCaptor.capture());
        MatchPaymentCommand matchCommand = matchCaptor.getValue();
        assertThat(matchCommand.paymentId()).isEqualTo(paymentId);
        assertThat(matchCommand.matchingMethod()).isEqualTo(MatchingMethod.NAME);
        assertThat(matchCommand.requestedAt()).isEqualTo(now);
    }
}
