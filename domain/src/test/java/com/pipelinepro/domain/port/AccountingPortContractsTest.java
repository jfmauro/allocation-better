package com.pipelinepro.domain.port;

import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.SourceAggregateType;
import com.pipelinepro.domain.port.in.AccountingEntryQueryUseCase;
import com.pipelinepro.domain.port.out.AccountingEntryRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountingPortContractsTest {

    @Test
    void should_return_newest_first_when_querying_accounting_repository_by_criteria() {
        InMemoryAccountingEntryRepository repository = new InMemoryAccountingEntryRepository();
        AccountingEntry first = newEntry(
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                Instant.parse("2026-07-10T08:00:00Z"));
        AccountingEntry second = newEntry(
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                Instant.parse("2026-07-10T10:00:00Z"));

        repository.append(first);
        repository.append(second);

        List<AccountingEntry> entries = repository.findByCriteria(
                Optional.empty(),
                Optional.of(LocalDate.of(2026, 7, 10)),
                Optional.of(LocalDate.of(2026, 7, 10)));

        assertThat(entries).containsExactly(second, first);
    }

    @Test
    void should_allow_fake_query_use_case_when_wiring_inbound_contract() {
        InMemoryAccountingEntryRepository repository = new InMemoryAccountingEntryRepository();
        AccountingEntryQueryUseCase queryUseCase = repository::findByCriteria;
        AccountingEntry allocationEntry = newEntry(
                AccountingEventType.PAYMENT_ALLOCATION,
                SourceAggregateType.ALLOCATION,
                Instant.parse("2026-07-10T12:00:00Z"));
        repository.append(allocationEntry);

        List<AccountingEntry> result = queryUseCase.listAccountingEntries(
                Optional.empty(),
                Optional.of(LocalDate.of(2026, 7, 10)),
                Optional.of(LocalDate.of(2026, 7, 10)));

        assertThat(result).containsExactly(allocationEntry);
    }

    private static AccountingEntry newEntry(
            AccountingEventType eventType,
            SourceAggregateType sourceAggregateType,
            Instant occurredAt) {
        return AccountingEntry.append(
                UUID.randomUUID(),
                eventType,
                sourceAggregateType,
                UUID.randomUUID(),
                new BigDecimal("15.00"),
                "EUR",
                occurredAt,
                occurredAt.plusSeconds(1));
    }

    private static final class InMemoryAccountingEntryRepository implements AccountingEntryRepository {
        private final List<AccountingEntry> storage = new ArrayList<>();

        @Override
        public AccountingEntry append(AccountingEntry accountingEntry) {
            storage.add(accountingEntry);
            return accountingEntry;
        }

        @Override
        public List<AccountingEntry> findByCriteria(
                Optional<AccountingEventType> eventType,
                Optional<LocalDate> fromDate,
                Optional<LocalDate> toDate) {
            return storage.stream()
                    .filter(entry -> eventType.map(type -> type == entry.eventType()).orElse(true))
                    .filter(entry -> fromDate
                            .map(from -> !entry.occurredAt().atOffset(ZoneOffset.UTC).toLocalDate().isBefore(from))
                            .orElse(true))
                    .filter(entry -> toDate
                            .map(to -> !entry.occurredAt().atOffset(ZoneOffset.UTC).toLocalDate().isAfter(to))
                            .orElse(true))
                    .sorted(Comparator.comparing(AccountingEntry::occurredAt).reversed())
                    .toList();
        }
    }
}
