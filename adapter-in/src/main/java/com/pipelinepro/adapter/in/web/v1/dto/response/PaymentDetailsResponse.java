package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentDetailsResponse(
        UUID id,
        String bankTransactionReference,
        BigDecimal amount,
        BigDecimal remainingAmount,
        String currency,
        Instant executionDate,
        Instant valueDate,
        PaymentStatus status,
        String structuredCommunication,
        String freeCommunication,
        String payerName,
        String payerIbanMasked,
        Long version,
        Instant createdAt,
        Instant updatedAt) {
}
