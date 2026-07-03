package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.ProposalStatus;

import java.time.Instant;
import java.util.UUID;

public record AllocationProposalSummaryResponse(
        UUID id,
        ProposalStatus status,
        MatchingMethod matchingMethod,
        String reason,
        Instant createdAt,
        Instant updatedAt) {
}
