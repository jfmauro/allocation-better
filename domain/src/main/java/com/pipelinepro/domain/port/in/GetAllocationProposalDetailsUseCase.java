package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.AllocationProposalDetails;

import java.util.Optional;
import java.util.UUID;

public interface GetAllocationProposalDetailsUseCase {
    Optional<AllocationProposalDetails> getProposalDetails(UUID proposalId);
}
