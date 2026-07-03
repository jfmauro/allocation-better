package com.pipelinepro.domain.port.in.command;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SelectDebtCommand(UUID proposalId, UUID debtId, String actor, Instant occurredAt) {
    public SelectDebtCommand {
        Objects.requireNonNull(proposalId, "proposalId");
        Objects.requireNonNull(debtId, "debtId");
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("actor must be non-blank");
        }
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
