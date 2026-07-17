package com.pipelinepro.domain;

import java.util.Objects;

public record AllocationProposalCandidateDetails(
        AllocationProposalCandidate candidate,
        Debt debt,
        Debtor debtor) {

    public AllocationProposalCandidateDetails {
        Objects.requireNonNull(candidate, "candidate");
    }
}
