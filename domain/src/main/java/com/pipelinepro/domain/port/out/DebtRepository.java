package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.Debt;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DebtRepository {
    Debt save(Debt debt);

    Optional<Debt> findById(UUID debtId);

    List<Debt> findByDebtorId(UUID debtorId);

    List<Debt> findByDebtorIds(Set<UUID> debtorIds);

    List<Debt> findByReference(String reference);
}
