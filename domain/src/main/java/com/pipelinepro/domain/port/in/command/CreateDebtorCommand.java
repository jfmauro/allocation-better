package com.pipelinepro.domain.port.in.command;

import com.pipelinepro.domain.DebtorType;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record CreateDebtorCommand(
        DebtorType debtorType,
        String displayName,
        String nationalNumber,
        String enterpriseNumber,
        String idempotencyKey,
        String correlationId) {

    private static final Logger log = LoggerFactory.getLogger(CreateDebtorCommand.class);

    public CreateDebtorCommand {
        log.info("+++start CreateDebtorCommand ctor+++");
        Objects.requireNonNull(debtorType, "debtorType");
        displayName = requireNotBlank(displayName, "displayName");
        idempotencyKey = requireNotBlank(idempotencyKey, "idempotencyKey");
        correlationId = requireNotBlank(correlationId, "correlationId");

        nationalNumber = normalizeNullable(nationalNumber);
        enterpriseNumber = normalizeNullable(enterpriseNumber);

        if (debtorType == DebtorType.NATURAL_PERSON && nationalNumber == null) {
            throw new IllegalArgumentException("Natural person debtor requires nationalNumber");
        }
        if (debtorType == DebtorType.NATURAL_PERSON && enterpriseNumber != null) {
            throw new IllegalArgumentException("Natural person debtor cannot define enterpriseNumber");
        }
        if (debtorType == DebtorType.ENTERPRISE && enterpriseNumber == null) {
            throw new IllegalArgumentException("Enterprise debtor requires enterpriseNumber");
        }
        if (debtorType == DebtorType.ENTERPRISE && nationalNumber != null) {
            throw new IllegalArgumentException("Enterprise debtor cannot define nationalNumber");
        }
        log.info("+++end CreateDebtorCommand ctor+++");
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
