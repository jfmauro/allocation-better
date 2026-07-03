package com.pipelinepro.domain.port.in.command;

import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.policy.AllocatableDebtStatusPolicy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record CreateDebtCommand(
        UUID debtorId,
        String reference,
        BigDecimal originalAmount,
        String currency,
        DebtStatus openingStatus,
        LocalDate dueDate,
        String idempotencyKey,
        String correlationId) {

    private static final Logger log = LoggerFactory.getLogger(CreateDebtCommand.class);

    public CreateDebtCommand {
        log.info("+++start CreateDebtCommand ctor+++");
        Objects.requireNonNull(debtorId, "debtorId");
        reference = requireNotBlank(reference, "reference");
        Objects.requireNonNull(originalAmount, "originalAmount");
        if (originalAmount.signum() <= 0) {
            throw new IllegalArgumentException("originalAmount must be > 0");
        }
        currency = requireNotBlank(currency, "currency");
        Objects.requireNonNull(openingStatus, "openingStatus");
        AllocatableDebtStatusPolicy.requireAllocatable(openingStatus);
        idempotencyKey = requireNotBlank(idempotencyKey, "idempotencyKey");
        correlationId = requireNotBlank(correlationId, "correlationId");
        log.info("+++end CreateDebtCommand ctor+++");
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value.trim();
    }
}
