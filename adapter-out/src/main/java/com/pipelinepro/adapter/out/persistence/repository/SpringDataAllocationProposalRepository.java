package com.pipelinepro.adapter.out.persistence.repository;

import com.pipelinepro.adapter.out.persistence.entity.AllocationProposalEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAllocationProposalRepository extends JpaRepository<AllocationProposalEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select proposal from AllocationProposalEntity proposal where proposal.id = :proposalId")
    Optional<AllocationProposalEntity> findByIdForUpdate(@Param("proposalId") UUID proposalId);

    List<AllocationProposalEntity> findByPaymentId(UUID paymentId);
}
