package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface QueryDebtUseCase {
    Optional<Debt> getDebt(UUID debtId);

    List<Debt> getDebts(Set<UUID> debtIds);

    List<Debt> listDebtsByDebtor(UUID debtorId, List<DebtStatus> statuses);
}
