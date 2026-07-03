package com.pipelinepro.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Debtor {
    private static final Logger log = LoggerFactory.getLogger(Debtor.class);
    private final UUID id;
    private final DebtorType type;
    private final String displayName;
    private final String nationalNumber;
    private final String enterpriseNumber;
    private boolean active;
    private final Instant createdAt;

    public Debtor(UUID id,
                  DebtorType type,
                   String displayName,
                   String nationalNumber,
                  String enterpriseNumber,
                  boolean active,
                  Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.displayName = requireNotBlank(displayName, "displayName");
        this.nationalNumber = normalizeNullable(nationalNumber);
        this.enterpriseNumber = normalizeNullable(enterpriseNumber);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        requireIdentifiersByType();
        guardIdentifierConsistency();
    }

    public static Debtor activeNaturalPerson(UUID id,
                                             String displayName,
                                              String nationalNumber,
                                             Instant createdAt) {
        log.info("+++start activeNaturalPerson+++ ");
        Debtor debtor = new Debtor(id, DebtorType.NATURAL_PERSON, displayName, nationalNumber, null, true, createdAt);
        log.info("+++end activeNaturalPerson+++ ");
        return debtor;
    }

    public static Debtor activeEnterprise(UUID id,
                                          String displayName,
                                          String enterpriseNumber,
                                          Instant createdAt) {
        log.info("+++start activeEnterprise+++ ");
        Debtor debtor = new Debtor(id, DebtorType.ENTERPRISE, displayName, null, enterpriseNumber, true, createdAt);
        log.info("+++end activeEnterprise+++ ");
        return debtor;
    }

    public UUID id() {
        return id;
    }

    public DebtorType type() {
        return type;
    }

    public String displayName() {
        return displayName;
    }

    public Optional<String> nationalNumber() {
        return Optional.ofNullable(nationalNumber);
    }

    public Optional<String> enterpriseNumber() {
        return Optional.ofNullable(enterpriseNumber);
    }

    public boolean active() {
        return active;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public void deactivate() {
        log.info("+++start deactivate+++ ");
        active = false;
        log.info("+++end deactivate+++ ");
    }

    private void requireIdentifiersByType() {
        if (type == DebtorType.NATURAL_PERSON && nationalNumber == null) {
            throw new IllegalStateException("Natural person debtor requires nationalNumber");
        }
        if (type == DebtorType.ENTERPRISE && enterpriseNumber == null) {
            throw new IllegalStateException("Enterprise debtor requires enterpriseNumber");
        }
    }

    private void guardIdentifierConsistency() {
        if (type == DebtorType.NATURAL_PERSON && enterpriseNumber != null) {
            throw new IllegalStateException("Natural person debtor cannot define enterpriseNumber");
        }
        if (type == DebtorType.ENTERPRISE && nationalNumber != null) {
            throw new IllegalStateException("Enterprise debtor cannot define nationalNumber");
        }
    }

    private static String requireNotBlank(String v, String name) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
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
