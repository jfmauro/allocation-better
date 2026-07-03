package com.pipelinepro.adapter.in.web.v1.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AllocationResultResponse(
        UUID allocationId,
        UUID proposalId,
        UUID paymentId,
        UUID debtId,
        BigDecimal amount,
        String status,
        String createdBy,
        Instant createdAt) {
}
