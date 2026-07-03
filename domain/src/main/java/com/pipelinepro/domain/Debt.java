package com.pipelinepro.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Debt {
    private static final Logger log = LoggerFactory.getLogger(Debt.class);
    private final UUID id;
    private final UUID debtorId;
    private final String reference;
    private final BigDecimal originalAmount;
    private BigDecimal remainingAmount;
    private final String currency;
    private DebtStatus status;
    private final LocalDate dueDate;
    private final Long version;
    private final Instant createdAt;
    private Instant updatedAt;

    public Debt(UUID id,
                UUID debtorId,
                String reference,
                BigDecimal originalAmount,
                String currency,
                DebtStatus status,
                LocalDate dueDate,
                BigDecimal remainingAmount,
                Long version,
                Instant createdAt,
                Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.debtorId = Objects.requireNonNull(debtorId, "debtorId");
        this.reference = requireNotBlank(reference, "reference");
        this.originalAmount = requirePositive(originalAmount, "originalAmount");
        this.currency = requireNotBlank(currency, "currency");
        this.status = Objects.requireNonNull(status, "status");
        this.dueDate = dueDate;
        this.remainingAmount = requireNonNegative(remainingAmount, "remainingAmount");
        this.version = Objects.requireNonNull(version, "version");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        guardRemainingNotExceedOriginal();
    }

    public static Debt open(UUID id, UUID debtorId, String reference, BigDecimal originalAmount, String currency) {
        Instant now = Instant.now();
        return open(id, debtorId, reference, originalAmount, currency, null, now);
    }

    public static Debt open(UUID id,
                            UUID debtorId,
                            String reference,
                            BigDecimal originalAmount,
                            String currency,
                            LocalDate dueDate,
                            Instant now) {
        log.info("+++start open+++ ");
        Debt debt = new Debt(id, debtorId, reference, originalAmount, currency, DebtStatus.OPEN, dueDate, originalAmount, 0L, now, now);
        log.info("+++end open+++ ");
        return debt;
    }

    public UUID id() {
        return id;
    }

    public UUID debtorId() {
        return debtorId;
    }

    public String reference() {
        return reference;
    }

    public BigDecimal originalAmount() {
        return originalAmount;
    }

    public BigDecimal remainingAmount() {
        return remainingAmount;
    }

    public String currency() {
        return currency;
    }

    public Optional<LocalDate> dueDate() {
        return Optional.ofNullable(dueDate);
    }

    public DebtStatus status() {
        return status;
    }

    public Long version() {
        return version;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void pay(BigDecimal amountPaid, Instant now) {
        log.info("+++start pay+++ ");
        requireNonNull(amountPaid, "amountPaid");
        requireNonNull(now, "now");
        if (status == DebtStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled debt cannot be paid");
        }
        if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amountPaid must be > 0");
        }
        if (amountPaid.compareTo(remainingAmount) > 0) {
            throw new IllegalStateException("Cannot pay more than remainingAmount");
        }
        remainingAmount = remainingAmount.subtract(amountPaid);
        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            status = DebtStatus.PAID;
        } else {
            status = DebtStatus.PARTIALLY_PAID;
        }
        updatedAt = now;
        log.info("+++end pay+++ ");
    }

    public void cancel(Instant now) {
        log.info("+++start cancel+++ ");
        requireNonNull(now, "now");
        if (status == DebtStatus.PAID) {
            throw new IllegalStateException("Paid debt cannot be cancelled");
        }
        status = DebtStatus.CANCELLED;
        updatedAt = now;
        log.info("+++end cancel+++ ");
    }

    private void guardRemainingNotExceedOriginal() {
        if (remainingAmount.compareTo(originalAmount) > 0) {
            throw new IllegalStateException("remainingAmount cannot exceed originalAmount");
        }
    }

    private static <T> T requireNonNull(T v, String name) {
        return Objects.requireNonNull(v, name);
    }

    private static String requireNotBlank(String v, String name) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return v;
    }

    private static BigDecimal requirePositive(BigDecimal v, String name) {
        requireNonNull(v, name);
        if (v.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return v;
    }

    private static BigDecimal requireNonNegative(BigDecimal v, String name) {
        requireNonNull(v, name);
        if (v.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
        return v;
    }
}
