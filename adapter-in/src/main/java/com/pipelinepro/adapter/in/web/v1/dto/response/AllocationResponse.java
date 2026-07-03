package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.AllocationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AllocationResponse(
        UUID id,
        UUID paymentId,
        UUID debtId,
        UUID proposalId,
        BigDecimal amount,
        AllocationStatus status,
        String idempotencyKey,
        String commandId,
        String createdBy,
        Instant createdAt) {
}
