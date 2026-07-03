package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.PaymentAllocation;

import java.util.Optional;
import java.util.UUID;

public interface GetAllocationDetailUseCase {
    Optional<PaymentAllocation> getAllocation(UUID allocationId);
}
