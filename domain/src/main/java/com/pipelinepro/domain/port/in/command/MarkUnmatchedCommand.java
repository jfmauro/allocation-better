package com.pipelinepro.domain.port.in.command;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record MarkUnmatchedCommand(UUID proposalId, String actor, String reason, Instant occurredAt) {
    public MarkUnmatchedCommand {
        Objects.requireNonNull(proposalId, "proposalId");
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("actor must be non-blank");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must be non-blank");
        }
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
