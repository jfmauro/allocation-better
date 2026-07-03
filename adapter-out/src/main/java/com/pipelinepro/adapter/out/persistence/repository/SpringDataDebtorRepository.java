package com.pipelinepro.adapter.out.persistence.repository;

import com.pipelinepro.adapter.out.persistence.entity.DebtorEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDebtorRepository extends JpaRepository<DebtorEntity, UUID> {
    Optional<DebtorEntity> findByNationalNumber(String nationalNumber);

    Optional<DebtorEntity> findByEnterpriseNumber(String enterpriseNumber);

    boolean existsByNationalNumber(String nationalNumber);

    boolean existsByEnterpriseNumber(String enterpriseNumber);

    List<DebtorEntity> findByActiveTrue();

    List<DebtorEntity> findAll();
}
