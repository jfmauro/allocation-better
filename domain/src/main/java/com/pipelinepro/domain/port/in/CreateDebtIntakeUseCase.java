package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.port.in.command.CreateDebtCommand;

public interface CreateDebtIntakeUseCase {
    Debt createDebt(CreateDebtCommand command);
}
