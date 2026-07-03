package com.pipelinepro.domain.policy;

import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DebtorIdPolicy {
    private static final Logger log = LoggerFactory.getLogger(DebtorIdPolicy.class);

    private DebtorIdPolicy() {
    }

    public static UUID newServerGeneratedId() {
        log.info("+++start newServerGeneratedId+++");
        UUID debtorId = UUID.randomUUID();
        if (debtorId.version() != 4) {
            throw new IllegalStateException("Server-generated debtorId must be UUID v4");
        }
        log.info("+++end newServerGeneratedId+++");
        return debtorId;
    }

    public static UUID requireCanonicalLowercaseV4(String rawDebtorId) {
        log.info("+++start requireCanonicalLowercaseV4+++");
        Objects.requireNonNull(rawDebtorId, "rawDebtorId");
        UUID parsed;
        try {
            parsed = UUID.fromString(rawDebtorId);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("debtorId must be a canonical UUID string", ex);
        }
        if (!parsed.toString().equals(rawDebtorId)) {
            throw new IllegalArgumentException("debtorId must be canonical lowercase UUID");
        }
        if (parsed.version() != 4) {
            throw new IllegalArgumentException("debtorId must be UUID v4");
        }
        log.info("+++end requireCanonicalLowercaseV4+++");
        return parsed;
    }

    public static void requireImmutable(UUID existingDebtorId, UUID candidateDebtorId) {
        log.info("+++start requireImmutable+++");
        Objects.requireNonNull(existingDebtorId, "existingDebtorId");
        Objects.requireNonNull(candidateDebtorId, "candidateDebtorId");
        if (!existingDebtorId.equals(candidateDebtorId)) {
            throw new IllegalStateException("debtorId is immutable");
        }
        log.info("+++end requireImmutable+++");
    }
}
