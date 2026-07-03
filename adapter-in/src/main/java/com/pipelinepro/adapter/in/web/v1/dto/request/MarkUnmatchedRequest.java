package com.pipelinepro.adapter.in.web.v1.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record MarkUnmatchedRequest(
        @NotBlank(message = "actor is required")
        String actor,
        @NotBlank(message = "reason is required")
        String reason,
        Instant occurredAt) {
}
