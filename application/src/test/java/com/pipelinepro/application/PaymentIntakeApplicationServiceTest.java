package com.pipelinepro.application;

import com.pipelinepro.application.port.out.PaymentIntakeTransactionalWorker;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentIntakeApplicationServiceTest {

    @Test
    void should_reject_null_value_date_at_command_boundary() {
        Instant receivedAt = Instant.parse("2026-01-01T10:15:30Z");

        assertThatThrownBy(() -> new ReceivePaymentCommand(
                UUID.randomUUID(),
                "TX-NULL-VALUE-DATE",
                receivedAt,
                null,
                new BigDecimal("100.00"),
                "EUR",
                null,
                null,
                null,
                "Payer",
                "BE**1234",
                receivedAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("valueDate must not be null");
    }

    @Test
    void should_delegate_receive_payment_to_transactional_worker() {
        PaymentIntakeTransactionalWorker worker = mock(PaymentIntakeTransactionalWorker.class);
        PaymentIntakeApplicationService service = new PaymentIntakeApplicationService(worker);
        ReceivePaymentCommand command = validCommand();
        Payment expectedPayment = Payment.received(
                command.paymentId(),
                command.bankTransactionReference(),
                command.amount(),
                command.currency(),
                command.structuredCommunication(),
                command.freeCommunication(),
                command.payerName(),
                command.payerIbanMasked(),
                command.receivedAt());
        when(worker.receivePayment(command)).thenReturn(expectedPayment);

        Payment actualPayment = service.receivePayment(command);

        assertThat(actualPayment).isSameAs(expectedPayment);
        verify(worker).receivePayment(command);
    }

    @Test
    void should_propagate_transactional_worker_exception() {
        PaymentIntakeTransactionalWorker worker = mock(PaymentIntakeTransactionalWorker.class);
        PaymentIntakeApplicationService service = new PaymentIntakeApplicationService(worker);
        ReceivePaymentCommand command = validCommand();
        RuntimeException failure = new IllegalStateException("payment intake failed");
        when(worker.receivePayment(command)).thenThrow(failure);

        assertThatThrownBy(() -> service.receivePayment(command))
                .isSameAs(failure);

        verify(worker).receivePayment(command);
    }

    @Test
    void should_propagate_duplicate_bank_transaction_rejection_from_worker() {
        PaymentIntakeTransactionalWorker worker = mock(PaymentIntakeTransactionalWorker.class);
        PaymentIntakeApplicationService service = new PaymentIntakeApplicationService(worker);
        ReceivePaymentCommand command = validCommand();
        IllegalStateException duplicate = new IllegalStateException(
                "Payment already exists for bankTransactionReference: " + command.bankTransactionReference());
        when(worker.receivePayment(command)).thenThrow(duplicate);

        assertThatThrownBy(() -> service.receivePayment(command))
                .isSameAs(duplicate);

        verify(worker).receivePayment(command);
    }

    private ReceivePaymentCommand validCommand() {
        Instant now = Instant.parse("2026-01-01T10:15:30Z");
        return new ReceivePaymentCommand(
                UUID.randomUUID(),
                "TX-VALID-1",
                now,
                now,
                new BigDecimal("100.00"),
                "EUR",
                "+++123/4567/89012+++",
                "free text",
                null,
                "Payer",
                "BE**1234",
                now);
    }
}
