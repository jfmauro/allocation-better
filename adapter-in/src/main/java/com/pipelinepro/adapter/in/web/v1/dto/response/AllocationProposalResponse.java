package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.ProposalStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AllocationProposalResponse(
        UUID id,
        UUID paymentId,
        ProposalStatus status,
        MatchingMethod matchingMethod,
        String reason,
        String validatedBy,
        Instant validatedAt,
        UUID selectedDebtId,
        List<AllocationProposalCandidateResponse> candidates,
        Long version,
        Instant createdAt,
        Instant updatedAt) {
}
