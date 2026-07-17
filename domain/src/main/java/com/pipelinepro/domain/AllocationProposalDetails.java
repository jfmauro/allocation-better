package com.pipelinepro.domain;

import java.util.List;
import java.util.Objects;

public record AllocationProposalDetails(
        AllocationProposal proposal,
        List<AllocationProposalCandidateDetails> candidates) {

    public AllocationProposalDetails {
        Objects.requireNonNull(proposal, "proposal");
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }
}
