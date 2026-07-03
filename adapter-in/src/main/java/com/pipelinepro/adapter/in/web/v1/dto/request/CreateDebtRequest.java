package com.pipelinepro.adapter.in.web.v1.dto.request;

import com.pipelinepro.domain.DebtStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateDebtRequest(
        @NotNull(message = "debtorId is required")
        UUID debtorId,
        @NotBlank(message = "reference is required")
        String reference,
        @NotNull(message = "originalAmount is required")
        @DecimalMin(value = "0.01", message = "originalAmount must be greater than zero")
        BigDecimal originalAmount,
        @NotBlank(message = "currency is required")
        String currency,
        @NotNull(message = "openingStatus is required")
        DebtStatus openingStatus,
        LocalDate dueDate) {
}
