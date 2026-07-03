package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.DebtStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProposalCandidateDebtResponse(
        UUID id,
        UUID debtorId,
        String reference,
        BigDecimal originalAmount,
        BigDecimal remainingAmount,
        String currency,
        DebtStatus status,
        LocalDate dueDate,
        String structuredCommunication,
        String freeCommunication,
        Long version,
        Instant createdAt,
        Instant updatedAt) {
}
