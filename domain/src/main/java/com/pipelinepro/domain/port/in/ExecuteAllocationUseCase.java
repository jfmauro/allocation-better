package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.in.command.ExecuteAllocationCommand;

public interface ExecuteAllocationUseCase {
    PaymentAllocation executeAllocation(ExecuteAllocationCommand command);
}
