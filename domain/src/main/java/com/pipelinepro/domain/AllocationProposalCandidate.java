package com.pipelinepro.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record AllocationProposalCandidate(
        UUID id,
        UUID proposalId,
        UUID debtorId,
        UUID debtId,
        MatchConfidence confidence,
        BigDecimal suggestedAmount,
        int rankOrder) {

    public AllocationProposalCandidate {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(proposalId, "proposalId");
        Objects.requireNonNull(debtorId, "debtorId");
        Objects.requireNonNull(debtId, "debtId");
        Objects.requireNonNull(confidence, "confidence");
        Objects.requireNonNull(suggestedAmount, "suggestedAmount");
        if (suggestedAmount.signum() <= 0) {
            throw new IllegalArgumentException("suggestedAmount must be > 0");
        }
        if (rankOrder < 0) {
            throw new IllegalArgumentException("rankOrder must be >= 0");
        }
    }
}
