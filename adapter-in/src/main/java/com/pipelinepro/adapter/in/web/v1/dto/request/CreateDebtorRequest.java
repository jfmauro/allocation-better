package com.pipelinepro.adapter.in.web.v1.dto.request;

import com.pipelinepro.domain.DebtorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDebtorRequest(
        @NotNull(message = "debtorType is required")
        DebtorType debtorType,
        @NotBlank(message = "displayName is required")
        String displayName,
        String nationalNumber,
        String enterpriseNumber) {
}
