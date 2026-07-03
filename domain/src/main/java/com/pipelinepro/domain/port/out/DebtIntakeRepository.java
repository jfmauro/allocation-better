package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.Debt;
import java.util.UUID;

public interface DebtIntakeRepository {
    Debt save(Debt debt);

    boolean existsByReference(String reference);

    boolean debtorExists(UUID debtorId);
}
