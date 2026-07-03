package com.pipelinepro.adapter.out.persistence.repository;

import com.pipelinepro.adapter.out.persistence.entity.DebtEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataDebtRepository extends JpaRepository<DebtEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select debt from DebtEntity debt where debt.id = :debtId")
    Optional<DebtEntity> findByIdForUpdate(@Param("debtId") UUID debtId);

    List<DebtEntity> findByDebtorId(UUID debtorId);

    List<DebtEntity> findByDebtorIdIn(Set<UUID> debtorIds);

    List<DebtEntity> findByReference(String reference);

    boolean existsByReference(String reference);
}
