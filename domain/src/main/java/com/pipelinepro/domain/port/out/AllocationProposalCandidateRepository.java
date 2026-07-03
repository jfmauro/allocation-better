package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.AllocationProposalCandidate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AllocationProposalCandidateRepository {
    AllocationProposalCandidate save(AllocationProposalCandidate candidate);

    Optional<AllocationProposalCandidate> findById(UUID candidateId);

    List<AllocationProposalCandidate> findByProposalId(UUID proposalId);
}
