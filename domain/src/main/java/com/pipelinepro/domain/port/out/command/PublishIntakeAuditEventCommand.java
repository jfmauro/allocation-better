package com.pipelinepro.domain.port.out.command;

import com.pipelinepro.domain.port.out.IntakeAggregateType;
import com.pipelinepro.domain.port.out.IntakeAuditLifecycle;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record PublishIntakeAuditEventCommand(
        UUID eventId,
        IntakeAggregateType aggregateType,
        UUID aggregateId,
        IntakeAuditLifecycle lifecycle,
        String reasonCode,
        String correlationId,
        Instant occurredAt) {

    private static final Logger log = LoggerFactory.getLogger(PublishIntakeAuditEventCommand.class);

    public PublishIntakeAuditEventCommand {
        log.info("+++start PublishIntakeAuditEventCommand ctor+++");
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(aggregateType, "aggregateType");
        Objects.requireNonNull(aggregateId, "aggregateId");
        Objects.requireNonNull(lifecycle, "lifecycle");
        correlationId = requireNotBlank(correlationId, "correlationId");
        Objects.requireNonNull(occurredAt, "occurredAt");

        reasonCode = normalizeNullable(reasonCode);
        if (lifecycle == IntakeAuditLifecycle.REJECTED && reasonCode == null) {
            throw new IllegalArgumentException("reasonCode must be provided for REJECTED lifecycle");
        }
        if (lifecycle != IntakeAuditLifecycle.REJECTED && reasonCode != null) {
            throw new IllegalArgumentException("reasonCode is only allowed for REJECTED lifecycle");
        }
        log.info("+++end PublishIntakeAuditEventCommand ctor+++");
    }

    public String eventType() {
        log.info("+++start eventType+++");
        String eventType = aggregateType.name() + "_" + lifecycle.name();
        log.info("+++end eventType+++");
        return eventType;
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
