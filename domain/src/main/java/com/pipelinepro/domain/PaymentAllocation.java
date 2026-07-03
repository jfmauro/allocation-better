package com.pipelinepro.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PaymentAllocation {
    private static final Logger log = LoggerFactory.getLogger(PaymentAllocation.class);
    private final UUID id;
    private final UUID paymentId;
    private final UUID debtId;
    private final UUID proposalId;
    private final BigDecimal amount;
    private final AllocationStatus status;
    private final String idempotencyKey;
    private final String commandId;
    private final String createdBy;
    private final Instant createdAt;

    private PaymentAllocation(UUID id,
                              UUID paymentId,
                              UUID debtId,
                              UUID proposalId,
                              BigDecimal amount,
                              AllocationStatus status,
                              String idempotencyKey,
                              String commandId,
                              String createdBy,
                              Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId");
        this.debtId = Objects.requireNonNull(debtId, "debtId");
        this.proposalId = proposalId;
        this.amount = requirePositive(amount, "amount");
        this.status = Objects.requireNonNull(status, "status");
        this.idempotencyKey = requireNotBlank(idempotencyKey, "idempotencyKey");
        this.commandId = requireNotBlank(commandId, "commandId");
        this.createdBy = requireNotBlank(createdBy, "createdBy");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static PaymentAllocation execute(UUID id,
                                            Payment payment,
                                            Debt debt,
                                            UUID proposalId,
                                            BigDecimal amount,
                                            String idempotencyKey,
                                            String commandId,
                                            String createdBy,
                                            Instant now) {
        log.info("+++start execute+++ ");
        Objects.requireNonNull(payment, "payment");
        Objects.requireNonNull(debt, "debt");
        Objects.requireNonNull(now, "now");
        requireCurrencyAlignment(payment, debt);
        if (amount.compareTo(payment.remainingAmount()) > 0) {
            throw new IllegalStateException("Allocation amount exceeds payment remaining amount");
        }
        if (amount.compareTo(debt.remainingAmount()) > 0) {
            throw new IllegalStateException("Allocation amount exceeds debt remaining amount");
        }
        payment.allocate(amount, now);
        debt.pay(amount, now);
        PaymentAllocation allocation = new PaymentAllocation(
                id,
                payment.id(),
                debt.id(),
                proposalId,
                amount,
                AllocationStatus.ALLOCATED,
                idempotencyKey,
                commandId,
                createdBy,
                now);
        log.info("+++end execute+++ ");
        return allocation;
    }

    public UUID id() {
        return id;
    }

    public UUID paymentId() {
        return paymentId;
    }

    public UUID debtId() {
        return debtId;
    }

    public Optional<UUID> proposalId() {
        return Optional.ofNullable(proposalId);
    }

    public BigDecimal amount() {
        return amount;
    }

    public AllocationStatus status() {
        return status;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public String commandId() {
        return commandId;
    }

    public String createdBy() {
        return createdBy;
    }

    public Instant createdAt() {
        return createdAt;
    }

    private static void requireCurrencyAlignment(Payment payment, Debt debt) {
        if (!payment.currency().equals(debt.currency())) {
            throw new IllegalStateException("Payment and debt currencies must match");
        }
    }

    private static BigDecimal requirePositive(BigDecimal v, String name) {
        Objects.requireNonNull(v, name);
        if (v.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return v;
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value;
    }
}
