package com.pipelinepro.adapter.in.web.v1.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ValidateProposalRequest(
        @NotNull(message = "debtId is required")
        UUID debtId,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,
        @NotBlank(message = "actor is required")
        String actor,
        @NotBlank(message = "reason is required")
        String reason,
        Instant occurredAt) {
}
