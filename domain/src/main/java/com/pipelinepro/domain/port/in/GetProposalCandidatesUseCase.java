package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.AllocationProposalCandidate;

import java.util.List;
import java.util.UUID;

public interface GetProposalCandidatesUseCase {
    List<AllocationProposalCandidate> listCandidates(UUID proposalId);
}
