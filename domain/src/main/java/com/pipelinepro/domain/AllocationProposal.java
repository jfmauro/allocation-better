package com.pipelinepro.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AllocationProposal {
    private static final Logger log = LoggerFactory.getLogger(AllocationProposal.class);
    private final UUID id;
    private final UUID paymentId;
    private ProposalStatus status;
    private final MatchingMethod matchingMethod;
    private String reason;
    private String validatedBy;
    private Instant validatedAt;
    private UUID selectedDebtId;
    private final Long version;
    private final Instant createdAt;
    private Instant updatedAt;

    private AllocationProposal(UUID id,
                               UUID paymentId,
                               ProposalStatus status,
                               MatchingMethod matchingMethod,
                               String reason,
                               String validatedBy,
                               Instant validatedAt,
                               UUID selectedDebtId,
                               Long version,
                               Instant createdAt,
                               Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId");
        this.status = Objects.requireNonNull(status, "status");
        this.matchingMethod = Objects.requireNonNull(matchingMethod, "matchingMethod");
        this.reason = normalizeNullable(reason);
        this.validatedBy = normalizeNullable(validatedBy);
        this.validatedAt = validatedAt;
        this.selectedDebtId = selectedDebtId;
        this.version = Objects.requireNonNull(version, "version");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static AllocationProposal proposed(UUID id,
                                              UUID paymentId,
                                              MatchingMethod matchingMethod,
                                              String reason,
                                              Instant createdAt) {
        log.info("+++start proposed+++ ");
        AllocationProposal proposal = new AllocationProposal(
                id,
                paymentId,
                ProposalStatus.PROPOSED,
                matchingMethod,
                reason,
                null,
                null,
                null,
                0L,
                createdAt,
                createdAt);
        log.info("+++end proposed+++ ");
        return proposal;
    }

    public UUID id() {
        return id;
    }

    public UUID paymentId() {
        return paymentId;
    }

    public ProposalStatus status() {
        return status;
    }

    public MatchingMethod matchingMethod() {
        return matchingMethod;
    }

    public Optional<String> reason() {
        return Optional.ofNullable(reason);
    }

    public Optional<String> validatedBy() {
        return Optional.ofNullable(validatedBy);
    }

    public Optional<Instant> validatedAt() {
        return Optional.ofNullable(validatedAt);
    }

    public Optional<UUID> selectedDebtId() {
        return Optional.ofNullable(selectedDebtId);
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

    public void validate(String actor, Instant now) {
        log.info("+++start validate+++ ");
        requireOpenProposal(now);
        validatedBy = requireNotBlank(actor, "actor");
        validatedAt = now;
        status = ProposalStatus.VALIDATED;
        updatedAt = now;
        log.info("+++end validate+++ ");
    }

    public void reject(String actor, String reasonValue, Instant now) {
        log.info("+++start reject+++ ");
        requireOpenProposal(now);
        validatedBy = requireNotBlank(actor, "actor");
        validatedAt = now;
        reason = requireNotBlank(reasonValue, "reason");
        status = ProposalStatus.REJECTED;
        updatedAt = now;
        log.info("+++end reject+++ ");
    }

    public void selectDebt(String actor, UUID debtId, Instant now) {
        log.info("+++start selectDebt+++ ");
        requireOpenProposal(now);
        requireNotBlank(actor, "actor");
        selectedDebtId = Objects.requireNonNull(debtId, "debtId");
        updatedAt = now;
        log.info("+++end selectDebt+++ ");
    }

    public void markUnmatched(String actor, String reasonValue, Instant now) {
        log.info("+++start markUnmatched+++ ");
        requireOpenProposal(now);
        validatedBy = requireNotBlank(actor, "actor");
        validatedAt = now;
        reason = requireNotBlank(reasonValue, "reason");
        status = ProposalStatus.UNMATCHED;
        updatedAt = now;
        log.info("+++end markUnmatched+++ ");
    }

    public void requestInvestigation(String actor, String reasonValue, Instant now) {
        log.info("+++start requestInvestigation+++ ");
        requireOpenProposal(now);
        validatedBy = requireNotBlank(actor, "actor");
        validatedAt = now;
        reason = requireNotBlank(reasonValue, "reason");
        status = ProposalStatus.INVESTIGATION_REQUESTED;
        updatedAt = now;
        log.info("+++end requestInvestigation+++ ");
    }

    private void requireOpenProposal(Instant now) {
        Objects.requireNonNull(now, "now");
        if (status != ProposalStatus.PROPOSED) {
            throw new IllegalStateException("Only PROPOSED proposals can be changed");
        }
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value;
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
