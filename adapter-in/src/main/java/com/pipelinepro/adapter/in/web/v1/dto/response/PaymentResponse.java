package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String bankTransactionReference,
        BigDecimal amount,
        BigDecimal remainingAmount,
        String currency,
        PaymentStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
