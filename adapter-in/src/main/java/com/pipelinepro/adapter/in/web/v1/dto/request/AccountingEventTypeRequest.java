package com.pipelinepro.adapter.in.web.v1.dto.request;

import com.pipelinepro.domain.AccountingEventType;

public enum AccountingEventTypeRequest {
    DEBT_ARRIVAL,
    PAYMENT_ARRIVAL,
    PAYMENT_ALLOCATION;

    public AccountingEventType toDomain() {
        return AccountingEventType.valueOf(name());
    }
}
