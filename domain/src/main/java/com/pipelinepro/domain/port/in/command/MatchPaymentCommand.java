package com.pipelinepro.domain.port.in.command;

import com.pipelinepro.domain.MatchingMethod;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record MatchPaymentCommand(
        UUID paymentId,
        MatchingMethod matchingMethod,
        Instant requestedAt) {

    public MatchPaymentCommand {
        Objects.requireNonNull(paymentId, "paymentId");
        Objects.requireNonNull(matchingMethod, "matchingMethod");
        Objects.requireNonNull(requestedAt, "requestedAt");
    }
}
