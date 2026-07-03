package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.Debtor;

public interface DebtorIntakeRepository {
    Debtor save(Debtor debtor);

    boolean existsByNationalNumber(String nationalNumber);

    boolean existsByEnterpriseNumber(String enterpriseNumber);
}
