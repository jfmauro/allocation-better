package com.pipelinepro.adapter.in.web.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record SelectDebtRequest(
        @NotNull(message = "debtId is required")
        UUID debtId,
        @NotBlank(message = "actor is required")
        String actor,
        Instant occurredAt) {
}
