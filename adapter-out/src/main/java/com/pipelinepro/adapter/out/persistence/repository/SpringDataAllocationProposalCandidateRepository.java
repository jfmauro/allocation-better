package com.pipelinepro.adapter.out.persistence.repository;

import com.pipelinepro.adapter.out.persistence.entity.AllocationProposalCandidateEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAllocationProposalCandidateRepository extends JpaRepository<AllocationProposalCandidateEntity, UUID> {
    List<AllocationProposalCandidateEntity> findByProposalId(UUID proposalId);
}
