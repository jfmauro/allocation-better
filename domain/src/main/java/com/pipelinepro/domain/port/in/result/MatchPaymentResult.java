package com.pipelinepro.domain.port.in.result;

import com.pipelinepro.domain.MatchingMethod;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record MatchPaymentResult(
        MatchingMethod matchingMethod,
        String reason,
        Optional<UUID> proposalId,
        boolean autoAllocationExecuted) {

    public MatchPaymentResult {
        Objects.requireNonNull(matchingMethod, "matchingMethod");
        Objects.requireNonNull(reason, "reason");
        proposalId = proposalId == null ? Optional.empty() : proposalId;
    }

    public static MatchPaymentResult proposalCreated(
            MatchingMethod matchingMethod,
            String reason,
            UUID proposalId) {
        return new MatchPaymentResult(matchingMethod, reason, Optional.of(proposalId), false);
    }

    public static MatchPaymentResult autoAllocated(
            MatchingMethod matchingMethod,
            String reason) {
        return new MatchPaymentResult(matchingMethod, reason, Optional.empty(), true);
    }
}
