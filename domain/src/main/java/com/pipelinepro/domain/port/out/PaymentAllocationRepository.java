package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.PaymentAllocation;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAllocationRepository {
    PaymentAllocation save(PaymentAllocation paymentAllocation);

    Optional<PaymentAllocation> findById(UUID allocationId);

    Optional<PaymentAllocation> findByIdempotencyKey(String idempotencyKey);
}
