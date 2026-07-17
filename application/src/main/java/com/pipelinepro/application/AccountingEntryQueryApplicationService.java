package com.pipelinepro.application;

import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.port.in.AccountingEntryQueryUseCase;
import com.pipelinepro.domain.port.out.AccountingEntryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AccountingEntryQueryApplicationService implements AccountingEntryQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountingEntryQueryApplicationService.class);

    private final AccountingEntryRepository accountingEntryRepository;

    public AccountingEntryQueryApplicationService(AccountingEntryRepository accountingEntryRepository) {
        this.accountingEntryRepository = Objects.requireNonNull(
                accountingEntryRepository, "accountingEntryRepository");
    }

    @Override
    public List<AccountingEntry> listAccountingEntries(
            Optional<AccountingEventType> eventType,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate) {
        log.info("+++start listAccountingEntries+++");
        try {
            validateDateRange(fromDate, toDate);
            return accountingEntryRepository.findByCriteria(eventType, fromDate, toDate);
        } finally {
            log.info("+++end listAccountingEntries+++");
        }
    }

    private void validateDateRange(Optional<LocalDate> fromDate, Optional<LocalDate> toDate) {
        Objects.requireNonNull(fromDate, "fromDate");
        Objects.requireNonNull(toDate, "toDate");
        if (fromDate.isPresent() && toDate.isPresent() && fromDate.get().isAfter(toDate.get())) {
            throw new IllegalArgumentException("fromDate must be less than or equal to toDate");
        }
    }
}
