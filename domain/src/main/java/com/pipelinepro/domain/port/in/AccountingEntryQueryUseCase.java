package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountingEntryQueryUseCase {
    List<AccountingEntry> listAccountingEntries(
            Optional<AccountingEventType> eventType,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate);
}
