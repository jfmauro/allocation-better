package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.out.command.AllocationExecutionRequest;

public interface AllocationTransactionalWorker {
    PaymentAllocation executeAllocation(AllocationExecutionRequest request);
}
