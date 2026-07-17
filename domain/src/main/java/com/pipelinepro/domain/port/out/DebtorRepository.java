package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.Debtor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DebtorRepository {
    Optional<Debtor> findById(UUID debtorId);

    Optional<Debtor> findByNationalNumber(String nationalNumber);

    Optional<Debtor> findByEnterpriseNumber(String enterpriseNumber);

    List<Debtor> findByIds(Set<UUID> debtorIds);

    List<Debtor> findAllActive();

    List<Debtor> findAll();
}
