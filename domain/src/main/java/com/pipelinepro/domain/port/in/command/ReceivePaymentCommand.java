package com.pipelinepro.domain.port.in.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ReceivePaymentCommand(
        UUID paymentId,
        String bankTransactionReference,
        Instant executionDate,
        Instant valueDate,
        BigDecimal amount,
        String currency,
        String structuredCommunication,
        String freeCommunication,
        String rawBankMessage,
        String payerName,
        String payerIbanMasked,
        Instant receivedAt) {

    public ReceivePaymentCommand {
        Objects.requireNonNull(paymentId, "paymentId");
        requireNotBlank(bankTransactionReference, "bankTransactionReference");
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        requireNotBlank(currency, "currency");
        Objects.requireNonNull(receivedAt, "receivedAt");
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value;
    }
}
