package com.pipelinepro.domain;

public enum PaymentStatus {
    RECEIVED,
    MATCH_PROPOSED,
    TO_MATCH,
    MATCH_PROCESSED,
    PARTIALLY_ALLOCATED,
    UNMATCHED,
    INVESTIGATION_REQUIRED,
    ALLOCATED,
    CANCELLED
}
