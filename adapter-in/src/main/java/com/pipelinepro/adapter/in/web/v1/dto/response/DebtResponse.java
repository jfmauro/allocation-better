package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.DebtStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DebtResponse(
        UUID id,
        UUID debtorId,
        java.math.BigDecimal originalAmount,
        java.math.BigDecimal remainingAmount,
        String currency,
        DebtStatus status,
        LocalDate dueDate,
        String structuredCommunication,
        String freeCommunication,
        Long version,
        Instant createdAt,
        Instant updatedAt) {
}
