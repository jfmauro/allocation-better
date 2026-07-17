package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.in.command.DebtorSearchCriteria;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface QueryDebtorUseCase {
    List<Debtor> listDebtors(Set<UUID> debtorIds);

    List<Debtor> listDebtors(DebtorSearchCriteria criteria);
}
