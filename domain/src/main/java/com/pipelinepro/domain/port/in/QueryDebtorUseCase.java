package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.in.command.DebtorSearchCriteria;
import java.util.List;

public interface QueryDebtorUseCase {
    List<Debtor> listDebtors(DebtorSearchCriteria criteria);
}
