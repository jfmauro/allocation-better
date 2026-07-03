package com.pipelinepro.adapter.in.web.v1.dto.response;

import com.pipelinepro.domain.DebtorType;

import java.time.Instant;
import java.util.UUID;

public record DebtorResponse(
        UUID id,
        DebtorType type,
        String displayName,
        String nationalNumber,
        String enterpriseNumber,
        boolean active,
        Instant createdAt) {
}
