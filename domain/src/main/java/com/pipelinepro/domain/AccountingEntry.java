package com.pipelinepro.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AccountingEntry(
        UUID id,
        AccountingEventType eventType,
        SourceAggregateType sourceAggregateType,
        UUID sourceAggregateId,
        BigDecimal amount,
        String currency,
        Instant occurredAt,
        Instant createdAt) {

    public AccountingEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(sourceAggregateType, "sourceAggregateType");
        validateEventSourceMapping(eventType, sourceAggregateType);
        Objects.requireNonNull(sourceAggregateId, "sourceAggregateId");
        amount = requirePositive(amount, "amount");
        currency = normalizeCurrency(currency, "currency");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(createdAt, "createdAt");
        if (createdAt.isBefore(occurredAt)) {
            throw new IllegalArgumentException("createdAt must be greater than or equal to occurredAt");
        }
    }

    public static AccountingEntry append(
            UUID id,
            AccountingEventType eventType,
            SourceAggregateType sourceAggregateType,
            UUID sourceAggregateId,
            BigDecimal amount,
            String currency,
            Instant occurredAt,
            Instant createdAt) {
        return new AccountingEntry(
                id,
                eventType,
                sourceAggregateType,
                sourceAggregateId,
                amount,
                currency,
                occurredAt,
                createdAt);
    }

    private static BigDecimal requirePositive(BigDecimal value, String name) {
        Objects.requireNonNull(value, name);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return value;
    }

    private static String normalizeCurrency(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value.trim().toUpperCase();
    }

    private static void validateEventSourceMapping(
            AccountingEventType eventType,
            SourceAggregateType sourceAggregateType) {
        SourceAggregateType expectedSourceType = switch (eventType) {
            case DEBT_ARRIVAL -> SourceAggregateType.DEBT;
            case PAYMENT_ARRIVAL -> SourceAggregateType.PAYMENT;
            case PAYMENT_ALLOCATION -> SourceAggregateType.ALLOCATION;
        };
        if (sourceAggregateType != expectedSourceType) {
            throw new IllegalArgumentException("sourceAggregateType " + sourceAggregateType
                    + " is invalid for eventType " + eventType
                    + "; expected " + expectedSourceType);
        }
    }
}
