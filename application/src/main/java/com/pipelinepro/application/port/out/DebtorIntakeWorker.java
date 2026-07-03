package com.pipelinepro.application.port.out;

import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.in.command.CreateDebtorCommand;

public interface DebtorIntakeWorker {
    Debtor createDebtor(CreateDebtorCommand command);
}
