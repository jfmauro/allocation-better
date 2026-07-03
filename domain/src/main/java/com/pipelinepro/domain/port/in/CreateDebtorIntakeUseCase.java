package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.in.command.CreateDebtorCommand;

public interface CreateDebtorIntakeUseCase {
    Debtor createDebtor(CreateDebtorCommand command);
}
