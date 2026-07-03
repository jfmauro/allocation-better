package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.ProposalStatus;

import java.time.Instant;
import java.util.UUID;

public record ProposalStateResponse(
        UUID id,
        ProposalStatus status,
        String reason,
        String validatedBy,
        Instant validatedAt,
        UUID selectedDebtId,
        Instant updatedAt) {
}
