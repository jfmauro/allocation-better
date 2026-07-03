package com.pipelinepro.application.port.out;

import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.port.in.command.CreateDebtCommand;

public interface DebtIntakeWorker {
    Debt createDebt(CreateDebtCommand command);
}
