package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountingEntryRepository {
    AccountingEntry append(AccountingEntry accountingEntry);

    /**
     * Finds accounting entries matching the provided criteria.
     *
     * <p>Ordering contract: returned entries MUST be sorted by {@code occurredAt} descending
     * (newest first).
     */
    List<AccountingEntry> findByCriteria(
            Optional<AccountingEventType> eventType,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate);
}
