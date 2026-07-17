package com.pipelinepro.adapter.out.persistence.entity;

import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.SourceAggregateType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "accounting_entry",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_accounting_entry_event_type_source_aggregate_id",
                        columnNames = {"event_type", "source_aggregate_id"})
        },
        indexes = {
                @Index(name = "idx_accounting_entry_event_type", columnList = "event_type"),
                @Index(name = "idx_accounting_entry_occurred_at", columnList = "occurred_at"),
                @Index(name = "idx_accounting_entry_event_type_occurred_at", columnList = "event_type,occurred_at")
        })
public class AccountingEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private AccountingEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_aggregate_type", nullable = false, length = 40)
    private SourceAggregateType sourceAggregateType;

    @Column(name = "source_aggregate_id", nullable = false)
    private UUID sourceAggregateId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AccountingEventType getEventType() {
        return eventType;
    }

    public void setEventType(AccountingEventType eventType) {
        this.eventType = eventType;
    }

    public SourceAggregateType getSourceAggregateType() {
        return sourceAggregateType;
    }

    public void setSourceAggregateType(SourceAggregateType sourceAggregateType) {
        this.sourceAggregateType = sourceAggregateType;
    }

    public UUID getSourceAggregateId() {
        return sourceAggregateId;
    }

    public void setSourceAggregateId(UUID sourceAggregateId) {
        this.sourceAggregateId = sourceAggregateId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
