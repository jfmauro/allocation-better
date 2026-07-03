package com.pipelinepro.domain.port.in.command;

import com.pipelinepro.domain.DebtorType;

public record DebtorSearchCriteria(String query, DebtorType debtorType, Boolean active) {
}
