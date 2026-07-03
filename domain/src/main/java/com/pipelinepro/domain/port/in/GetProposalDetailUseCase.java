package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.AllocationProposal;

import java.util.Optional;
import java.util.UUID;

public interface GetProposalDetailUseCase {
    Optional<AllocationProposal> getProposal(UUID proposalId);
}
