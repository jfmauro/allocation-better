package com.pipelinepro.adapter.in.web.v1.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountingEntryResponse(
        String eventType,
        String sourceAggregateType,
        UUID sourceAggregateId,
        BigDecimal amount,
        String currency,
        Instant occurredAt,
        Instant createdAt) {
}
