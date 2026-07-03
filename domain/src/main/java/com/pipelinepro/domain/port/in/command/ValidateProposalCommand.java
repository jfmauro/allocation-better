package com.pipelinepro.domain.port.in.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ValidateProposalCommand(UUID proposalId, UUID debtId, BigDecimal amount, String actor, String reason, Instant occurredAt) {
    public ValidateProposalCommand {
        Objects.requireNonNull(proposalId, "proposalId");
        Objects.requireNonNull(debtId, "debtId");
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("actor must be non-blank");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must be non-blank");
        }
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
