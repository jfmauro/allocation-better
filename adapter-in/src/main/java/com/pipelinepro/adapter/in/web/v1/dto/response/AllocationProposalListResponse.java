package com.pipelinepro.adapter.in.web.v1.dto.response;

import java.util.List;
import java.util.UUID;

public record AllocationProposalListResponse(
        UUID paymentId,
        List<AllocationProposalSummaryResponse> proposals) {
}
