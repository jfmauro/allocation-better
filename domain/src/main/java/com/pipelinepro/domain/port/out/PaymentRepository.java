package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(UUID paymentId);

    Optional<Payment> findByBankTransactionReference(String bankTransactionReference);
}
