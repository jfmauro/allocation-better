package com.pipelinepro.domain.port.out.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AllocationExecutionRequest(
        UUID paymentId,
        UUID debtId,
        UUID proposalId,
        BigDecimal amount,
        String idempotencyKey,
        String commandId,
        String actor,
        Instant occurredAt) {

    public AllocationExecutionRequest {
        Objects.requireNonNull(paymentId, "paymentId");
        Objects.requireNonNull(debtId, "debtId");
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        requireNotBlank(idempotencyKey, "idempotencyKey");
        requireNotBlank(commandId, "commandId");
        requireNotBlank(actor, "actor");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value;
    }
}
