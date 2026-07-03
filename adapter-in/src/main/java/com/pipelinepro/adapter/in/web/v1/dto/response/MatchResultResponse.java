package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.MatchingMethod;

import java.util.UUID;

public record MatchResultResponse(
        UUID paymentId,
        MatchingMethod matchingMethod,
        String reason,
        UUID proposalId,
        boolean autoAllocationExecuted) {
}
