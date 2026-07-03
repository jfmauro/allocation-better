package com.pipelinepro.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AuditEvent(
        UUID id,
        String aggregateType,
        UUID aggregateId,
        String eventType,
        String actor,
        String payloadJson,
        Instant createdAt) {

    public AuditEvent {
        Objects.requireNonNull(id, "id");
        requireNotBlank(aggregateType, "aggregateType");
        Objects.requireNonNull(aggregateId, "aggregateId");
        requireNotBlank(eventType, "eventType");
        requireNotBlank(payloadJson, "payloadJson");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value;
    }
}
