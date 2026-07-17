package com.pipelinepro.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountingEntryTest {

    @Test
    void should_create_accounting_entry_when_payload_is_valid() {
        Instant occurredAt = Instant.parse("2026-07-10T09:00:00Z");
        Instant createdAt = Instant.parse("2026-07-10T09:05:00Z");
        UUID sourceAggregateId = UUID.randomUUID();

        AccountingEntry entry = AccountingEntry.append(
                UUID.randomUUID(),
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                sourceAggregateId,
                new BigDecimal("125.50"),
                "eur",
                occurredAt,
                createdAt);

        assertThat(entry.currency()).isEqualTo("EUR");
        assertThat(entry.sourceAggregateId()).isEqualTo(sourceAggregateId);
        assertThat(entry.eventType()).isEqualTo(AccountingEventType.PAYMENT_ARRIVAL);
    }

    @Test
    void should_reject_entry_when_amount_is_non_positive() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                UUID.randomUUID(),
                BigDecimal.ZERO,
                "EUR",
                Instant.parse("2026-07-10T09:00:00Z"),
                Instant.parse("2026-07-10T09:05:00Z")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount must be > 0");
    }

    @Test
    void should_reject_entry_when_created_at_is_before_occurred_at() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                AccountingEventType.PAYMENT_ALLOCATION,
                SourceAggregateType.ALLOCATION,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "EUR",
                Instant.parse("2026-07-10T09:05:00Z"),
                Instant.parse("2026-07-10T09:04:00Z")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("createdAt must be greater than or equal to occurredAt");
    }

    @Test
    void should_reject_entry_when_id_is_null() {
        assertThatThrownBy(() -> new AccountingEntry(
                null,
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "EUR",
                Instant.parse("2026-07-10T09:00:00Z"),
                Instant.parse("2026-07-10T09:05:00Z")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("id");
    }

    @Test
    void should_reject_entry_when_event_type_is_null() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                null,
                SourceAggregateType.DEBT,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "EUR",
                Instant.parse("2026-07-10T09:00:00Z"),
                Instant.parse("2026-07-10T09:05:00Z")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("eventType");
    }

    @Test
    void should_reject_entry_when_source_aggregate_type_is_null() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                AccountingEventType.DEBT_ARRIVAL,
                null,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "EUR",
                Instant.parse("2026-07-10T09:00:00Z"),
                Instant.parse("2026-07-10T09:05:00Z")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("sourceAggregateType");
    }

    @Test
    void should_reject_entry_when_source_aggregate_id_is_null() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                null,
                new BigDecimal("20.00"),
                "EUR",
                Instant.parse("2026-07-10T09:00:00Z"),
                Instant.parse("2026-07-10T09:05:00Z")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("sourceAggregateId");
    }

    @Test
    void should_reject_entry_when_amount_is_null() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                UUID.randomUUID(),
                null,
                "EUR",
                Instant.parse("2026-07-10T09:00:00Z"),
                Instant.parse("2026-07-10T09:05:00Z")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("amount");
    }

    @Test
    void should_reject_entry_when_currency_is_blank() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "   ",
                Instant.parse("2026-07-10T09:00:00Z"),
                Instant.parse("2026-07-10T09:05:00Z")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency must be non-blank");
    }

    @Test
    void should_reject_entry_when_occurred_at_is_null() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "EUR",
                null,
                Instant.parse("2026-07-10T09:05:00Z")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("occurredAt");
    }

    @Test
    void should_reject_entry_when_created_at_is_null() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "EUR",
                Instant.parse("2026-07-10T09:00:00Z"),
                null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("createdAt");
    }

    @Test
    void should_reject_entry_when_event_and_source_mapping_is_invalid() {
        assertThatThrownBy(() -> new AccountingEntry(
                UUID.randomUUID(),
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.DEBT,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "EUR",
                Instant.parse("2026-07-10T09:00:00Z"),
                Instant.parse("2026-07-10T09:05:00Z")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceAggregateType")
                .hasMessageContaining("PAYMENT_ARRIVAL")
                .hasMessageContaining("PAYMENT");
    }
}
