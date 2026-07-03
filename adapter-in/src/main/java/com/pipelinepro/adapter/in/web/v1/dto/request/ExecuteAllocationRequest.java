package com.pipelinepro.adapter.in.web.v1.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExecuteAllocationRequest(
        @NotNull(message = "paymentId is required")
        UUID paymentId,
        @NotNull(message = "debtId is required")
        UUID debtId,
        UUID proposalId,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,
        @NotBlank(message = "idempotencyKey is required")
        String idempotencyKey,
        @NotBlank(message = "commandId is required")
        String commandId,
        @NotBlank(message = "actor is required")
        String actor,
        Instant occurredAt) {
}
