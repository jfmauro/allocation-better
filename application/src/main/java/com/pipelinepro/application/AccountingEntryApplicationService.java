package com.pipelinepro.application;

import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.SourceAggregateType;
import com.pipelinepro.domain.port.out.AccountingEntryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AccountingEntryApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AccountingEntryApplicationService.class);

    private final AccountingEntryRepository accountingEntryRepository;

    public AccountingEntryApplicationService(AccountingEntryRepository accountingEntryRepository) {
        this.accountingEntryRepository = Objects.requireNonNull(
                accountingEntryRepository, "accountingEntryRepository");
    }

    public AccountingEntry appendAccountingEntry(
            AccountingEventType eventType,
            SourceAggregateType sourceAggregateType,
            UUID sourceAggregateId,
            BigDecimal amount,
            String currency,
            Instant occurredAt) {
        log.info("+++start appendAccountingEntry+++");
        try {
            AccountingEntry entry = AccountingEntry.append(
                    UUID.randomUUID(),
                    eventType,
                    sourceAggregateType,
                    sourceAggregateId,
                    amount,
                    currency,
                    occurredAt,
                    occurredAt);
            return accountingEntryRepository.append(entry);
        } finally {
            log.info("+++end appendAccountingEntry+++");
        }
    }

    public AccountingEntry append(AccountingEntry accountingEntry) {
        log.info("+++start append+++");
        try {
            return accountingEntryRepository.append(Objects.requireNonNull(accountingEntry, "accountingEntry"));
        } finally {
            log.info("+++end append+++");
        }
    }
}
