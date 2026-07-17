package com.pipelinepro.adapter.in.web.v1.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReceivePaymentRequest(
        UUID paymentId,
        @NotBlank(message = "bankTransactionReference is required")
        String bankTransactionReference,
        Instant executionDate,
        @NotNull(message = "valueDate is required")
        Instant valueDate,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,
        @NotBlank(message = "currency is required")
        @Pattern(regexp = "EUR", message = "currency must be EUR")
        String currency,
        String structuredCommunication,
        String freeCommunication,
        String rawBankMessage,
        String payerName,
        String payerIban,
        String payerIbanMasked,
        Instant receivedAt) {
}
