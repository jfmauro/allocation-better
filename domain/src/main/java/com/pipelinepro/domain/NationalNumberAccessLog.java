package com.pipelinepro.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record NationalNumberAccessLog(
        UUID id,
        UUID paymentId,
        UUID debtorId,
        String userId,
        String reason,
        Instant createdAt) {

    public NationalNumberAccessLog {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(debtorId, "debtorId");
        requireNotBlank(userId, "userId");
        requireNotBlank(reason, "reason");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public Optional<UUID> optionalPaymentId() {
        return Optional.ofNullable(paymentId);
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value;
    }
}
