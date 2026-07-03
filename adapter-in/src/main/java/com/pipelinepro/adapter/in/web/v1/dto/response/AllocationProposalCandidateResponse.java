package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.MatchConfidence;

import java.math.BigDecimal;
import java.util.UUID;

public record AllocationProposalCandidateResponse(
        UUID id,
        UUID debtorId,
        UUID debtId,
        MatchConfidence confidence,
        BigDecimal suggestedAmount,
        int rankOrder,
        ProposalCandidateDebtResponse debt,
        ProposalCandidateDebtorResponse debtor) {
}
