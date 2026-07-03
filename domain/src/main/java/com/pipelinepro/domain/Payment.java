package com.pipelinepro.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Payment {
    private static final Logger log = LoggerFactory.getLogger(Payment.class);
    private final UUID id;
    private final String bankTransactionReference;
    private final BigDecimal amount;
    private BigDecimal remainingAmount;
    private final String currency;
    private PaymentStatus status;
    private final String structuredCommunication;
    private final String freeCommunication;
    private final String payerName;
    private final String payerIbanMasked;
    private final Long version;
    private final Instant createdAt;
    private Instant updatedAt;

    private Payment(
            UUID id,
            String bankTransactionReference,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String structuredCommunication,
            String freeCommunication,
            String payerName,
            String payerIbanMasked,
            BigDecimal remainingAmount,
            Long version,
            Instant createdAt,
            Instant updatedAt) {
        this.id = requireNotNull(id, "id");
        this.bankTransactionReference = requireNotBlank(bankTransactionReference, "bankTransactionReference");
        this.amount = requirePositive(amount, "amount");
        this.currency = requireNotBlank(currency, "currency");
        this.status = requireNotNull(status, "status");
        this.structuredCommunication = normalizeNullable(structuredCommunication);
        this.freeCommunication = normalizeNullable(freeCommunication);
        this.payerName = normalizeNullable(payerName);
        this.payerIbanMasked = normalizeNullable(payerIbanMasked);
        this.remainingAmount = requireNonNegative(remainingAmount, "remainingAmount");
        this.version = requireNotNull(version, "version");
        this.createdAt = requireNotNull(createdAt, "createdAt");
        this.updatedAt = requireNotNull(updatedAt, "updatedAt");
        guardRemainingNotExceedAmount();
    }

    public static Payment received(UUID id, String bankTransactionReference, BigDecimal amount, String currency) {
        log.info("+++start received+++ ");
        Instant now = Instant.now();
        Payment payment = received(id, bankTransactionReference, amount, currency, null, null, null, null, now);
        log.info("+++end received+++ ");
        return payment;
    }

    public static Payment received(UUID id,
                                   String bankTransactionReference,
                                   BigDecimal amount,
                                   String currency,
                                   String structuredCommunication,
                                   String freeCommunication,
                                   String payerName,
                                   String payerIbanMasked,
                                   Instant now) {
        if (!"EUR".equals(currency)) {
            throw new IllegalArgumentException("currency must be EUR");
        }
        return new Payment(
                id,
                bankTransactionReference,
                amount,
                currency,
                PaymentStatus.RECEIVED,
                structuredCommunication,
                freeCommunication,
                payerName,
                payerIbanMasked,
                amount,
                0L,
                now,
                now);
    }

    public UUID id() {
        return id;
    }

    public String bankTransactionReference() {
        return bankTransactionReference;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BigDecimal remainingAmount() {
        return remainingAmount;
    }

    public String currency() {
        return currency;
    }

    public PaymentStatus status() {
        return status;
    }

    public Optional<String> structuredCommunication() {
        return Optional.ofNullable(structuredCommunication);
    }

    public Optional<String> freeCommunication() {
        return Optional.ofNullable(freeCommunication);
    }

    public Optional<String> payerName() {
        return Optional.ofNullable(payerName);
    }

    public Optional<String> payerIbanMasked() {
        return Optional.ofNullable(payerIbanMasked);
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

    public void markMatchProposed(Instant now) {
        log.info("+++start markMatchProposed+++ ");
        requireNotNull(now, "now");
        if (status != PaymentStatus.RECEIVED) {
            throw new IllegalStateException("Payment can be marked MATCH_PROPOSED only from RECEIVED");
        }
        status = PaymentStatus.MATCH_PROPOSED;
        updatedAt = now;
        log.info("+++end markMatchProposed+++ ");
    }

    public void markToMatch(Instant now) {
        log.info("+++start markToMatch+++ ");
        requireNotNull(now, "now");
        status = PaymentStatus.TO_MATCH;
        updatedAt = now;
        log.info("+++end markToMatch+++ ");
    }

    public void allocate(BigDecimal amountAllocated, Instant now) {
        log.info("+++start allocate+++ ");
        requireNotNull(amountAllocated, "amountAllocated");
        requireNotNull(now, "now");
        if (status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled payment cannot be allocated");
        }
        if (amountAllocated.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amountAllocated must be > 0");
        }
        if (amountAllocated.compareTo(remainingAmount) > 0) {
            throw new IllegalStateException("Cannot allocate more than remainingAmount");
        }
        remainingAmount = remainingAmount.subtract(amountAllocated);
        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            status = PaymentStatus.ALLOCATED;
        } else {
            status = PaymentStatus.PARTIALLY_ALLOCATED;
        }
        updatedAt = now;
        log.info("+++end allocate+++ ");
    }

    public void cancel(Instant now) {
        log.info("+++start cancel+++ ");
        requireNotNull(now, "now");
        if (status == PaymentStatus.ALLOCATED) {
            throw new IllegalStateException("Allocated payment cannot be cancelled");
        }
        status = PaymentStatus.CANCELLED;
        updatedAt = now;
        log.info("+++end cancel+++ ");
    }

    public void markUnmatched(Instant now) {
        log.info("+++start markUnmatched+++ ");
        requireNotNull(now, "now");
        status = PaymentStatus.UNMATCHED;
        updatedAt = now;
        log.info("+++end markUnmatched+++ ");
    }

    public void requestInvestigation(Instant now) {
        log.info("+++start requestInvestigation+++ ");
        requireNotNull(now, "now");
        status = PaymentStatus.INVESTIGATION_REQUIRED;
        updatedAt = now;
        log.info("+++end requestInvestigation+++ ");
    }

    private void guardRemainingNotExceedAmount() {
        if (remainingAmount.compareTo(amount) > 0) {
            throw new IllegalStateException("remainingAmount cannot exceed amount");
        }
    }

    private static <T> T requireNotNull(T v, String name) {
        return Objects.requireNonNull(v, name);
    }

    private static String requireNotBlank(String v, String name) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return v;
    }

    private static BigDecimal requirePositive(BigDecimal v, String name) {
        requireNotNull(v, name);
        if (v.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return v;
    }

    private static BigDecimal requireNonNegative(BigDecimal v, String name) {
        requireNotNull(v, name);
        if (v.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
        return v;
    }

    private static String normalizeNullable(String v) {
        if (v == null) {
            return null;
        }
        String trimmed = v.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
