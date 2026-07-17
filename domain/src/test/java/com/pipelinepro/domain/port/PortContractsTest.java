package com.pipelinepro.domain.port;

import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.in.ReceivePaymentUseCase;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import com.pipelinepro.domain.port.out.AllocationTransactionalWorker;
import com.pipelinepro.domain.port.out.PaymentRepository;
import com.pipelinepro.domain.port.out.command.AllocationExecutionRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PortContractsTest {

    @Test
    void should_allow_fake_payment_repository_for_inbound_use_case() {
        InMemoryPaymentRepository paymentRepository = new InMemoryPaymentRepository();
        ReceivePaymentUseCase useCase = command -> paymentRepository.save(Payment.received(
                command.paymentId(),
                command.bankTransactionReference(),
                command.amount(),
                command.currency(),
                command.structuredCommunication(),
                command.freeCommunication(),
                command.payerName(),
                command.payerIbanMasked(),
                command.receivedAt()));

        UUID paymentId = UUID.randomUUID();
        Payment payment = useCase.receivePayment(new ReceivePaymentCommand(
                paymentId,
                "TX-PORT-1",
                null,
                Instant.parse("2024-01-01T00:00:00Z"),
                new BigDecimal("50.00"),
                "EUR",
                null,
                null,
                null,
                "Payer",
                null,
                Instant.now()));

        assertThat(paymentRepository.findById(paymentId)).contains(payment);
    }

    @Test
    void should_allow_fake_transactional_worker_port() {
        Payment payment = Payment.received(UUID.randomUUID(), "TX-PORT-2", new BigDecimal("100.00"), "EUR");
        Debt debt = Debt.open(UUID.randomUUID(), UUID.randomUUID(), "D-PORT-1", new BigDecimal("100.00"), "EUR");

        AllocationTransactionalWorker worker = request -> PaymentAllocation.execute(
                UUID.randomUUID(),
                payment,
                debt,
                request.proposalId(),
                request.amount(),
                request.idempotencyKey(),
                request.commandId(),
                request.actor(),
                request.occurredAt());

        PaymentAllocation allocation = worker.executeAllocation(new AllocationExecutionRequest(
                payment.id(),
                debt.id(),
                UUID.randomUUID(),
                new BigDecimal("30.00"),
                "idem-port",
                "cmd-port",
                "port-user",
                Instant.now()));

        assertThat(allocation.amount()).isEqualByComparingTo("30.00");
        assertThat(payment.remainingAmount()).isEqualByComparingTo("70.00");
        assertThat(debt.remainingAmount()).isEqualByComparingTo("70.00");
    }

    private static final class InMemoryPaymentRepository implements PaymentRepository {
        private final Map<UUID, Payment> storage = new HashMap<>();

        @Override
        public Payment save(Payment payment) {
            storage.put(payment.id(), payment);
            return payment;
        }

        @Override
        public Optional<Payment> findById(UUID paymentId) {
            return Optional.ofNullable(storage.get(paymentId));
        }

        @Override
        public Optional<Payment> findByBankTransactionReference(String bankTransactionReference) {
            return storage.values().stream()
                    .filter(payment -> payment.bankTransactionReference().equals(bankTransactionReference))
                    .findFirst();
        }
    }
}
