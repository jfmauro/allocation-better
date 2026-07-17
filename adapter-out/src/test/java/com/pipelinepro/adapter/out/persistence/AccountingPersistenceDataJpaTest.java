package com.pipelinepro.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.pipelinepro.adapter.out.persistence.entity.AccountingEntryEntity;
import com.pipelinepro.adapter.out.persistence.mapper.AccountingEntryEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAccountingEntryRepository;
import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.SourceAggregateType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class AccountingPersistenceDataJpaTest {

    private final AccountingEntryEntityMapper accountingEntryEntityMapper =
            Mappers.getMapper(AccountingEntryEntityMapper.class);

    @Autowired
    private SpringDataAccountingEntryRepository springDataAccountingEntryRepository;

    @Test
    void should_round_trip_accounting_entry_mapping() {
        AccountingEntry entry = AccountingEntry.append(
                UUID.randomUUID(),
                AccountingEventType.PAYMENT_ALLOCATION,
                SourceAggregateType.ALLOCATION,
                UUID.randomUUID(),
                new BigDecimal("12.50"),
                "eur",
                Instant.parse("2026-07-10T10:00:00Z"),
                Instant.parse("2026-07-10T10:00:01Z"));

        AccountingEntryEntity entity = accountingEntryEntityMapper.toEntity(entry);
        entity.setId(null);
        AccountingEntryEntity saved = springDataAccountingEntryRepository.saveAndFlush(entity);

        assertThat(accountingEntryEntityMapper.toDomain(saved))
                .usingRecursiveComparison()
                .isEqualTo(AccountingEntry.append(
                        saved.getId(),
                        entry.eventType(),
                        entry.sourceAggregateType(),
                        entry.sourceAggregateId(),
                        entry.amount(),
                        entry.currency(),
                        entry.occurredAt(),
                        entry.createdAt()));
    }

    @Test
    void should_filter_by_event_type_newest_first() {
        AccountingEntryEntity older = entityOf(
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                Instant.parse("2026-07-10T08:00:00Z"));
        AccountingEntryEntity newer = entityOf(
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                Instant.parse("2026-07-10T10:00:00Z"));
        AccountingEntryEntity otherType = entityOf(
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                Instant.parse("2026-07-10T12:00:00Z"));

        springDataAccountingEntryRepository.saveAllAndFlush(List.of(older, newer, otherType));

        List<AccountingEntry> result = findByCriteria(
                Optional.of(AccountingEventType.DEBT_ARRIVAL),
                Optional.of(LocalDate.of(2026, 7, 10)),
                Optional.of(LocalDate.of(2026, 7, 10)));

        assertThat(result).extracting(AccountingEntry::occurredAt)
                .containsExactly(newer.getOccurredAt(), older.getOccurredAt());
    }

    @Test
    void should_filter_with_from_only_range_newest_first() {
        AccountingEntryEntity beforeFrom = entityOf(
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                Instant.parse("2026-07-09T23:59:59Z"));
        AccountingEntryEntity atFrom = entityOf(
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                Instant.parse("2026-07-10T00:00:00Z"));
        AccountingEntryEntity afterFrom = entityOf(
                AccountingEventType.PAYMENT_ALLOCATION,
                SourceAggregateType.ALLOCATION,
                Instant.parse("2026-07-11T10:00:00Z"));

        springDataAccountingEntryRepository.saveAllAndFlush(List.of(beforeFrom, atFrom, afterFrom));

        List<AccountingEntry> result = findByCriteria(
                Optional.empty(),
                Optional.of(LocalDate.of(2026, 7, 10)),
                Optional.empty());

        assertThat(result).extracting(AccountingEntry::occurredAt)
                .containsExactly(afterFrom.getOccurredAt(), atFrom.getOccurredAt());
    }

    @Test
    void should_filter_with_to_only_range_newest_first() {
        AccountingEntryEntity beforeTo = entityOf(
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                Instant.parse("2026-07-09T12:00:00Z"));
        AccountingEntryEntity atTo = entityOf(
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                Instant.parse("2026-07-10T23:59:59Z"));
        AccountingEntryEntity afterTo = entityOf(
                AccountingEventType.PAYMENT_ALLOCATION,
                SourceAggregateType.ALLOCATION,
                Instant.parse("2026-07-11T00:00:00Z"));

        springDataAccountingEntryRepository.saveAllAndFlush(List.of(beforeTo, atTo, afterTo));

        List<AccountingEntry> result = findByCriteria(
                Optional.empty(),
                Optional.empty(),
                Optional.of(LocalDate.of(2026, 7, 10)));

        assertThat(result).extracting(AccountingEntry::occurredAt)
                .containsExactly(atTo.getOccurredAt(), beforeTo.getOccurredAt());
    }

    @Test
    void should_include_range_boundaries_newest_first() {
        AccountingEntryEntity beforeRange = entityOf(
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                Instant.parse("2026-07-09T23:59:59Z"));
        AccountingEntryEntity atRangeStart = entityOf(
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                Instant.parse("2026-07-10T00:00:00Z"));
        AccountingEntryEntity atRangeEnd = entityOf(
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                Instant.parse("2026-07-10T23:59:59Z"));
        AccountingEntryEntity afterRange = entityOf(
                AccountingEventType.PAYMENT_ALLOCATION,
                SourceAggregateType.ALLOCATION,
                Instant.parse("2026-07-11T00:00:00Z"));

        springDataAccountingEntryRepository.saveAllAndFlush(List.of(beforeRange, atRangeStart, atRangeEnd, afterRange));

        List<AccountingEntry> result = findByCriteria(
                Optional.empty(),
                Optional.of(LocalDate.of(2026, 7, 10)),
                Optional.of(LocalDate.of(2026, 7, 10)));

        assertThat(result).extracting(AccountingEntry::occurredAt)
                .containsExactly(atRangeEnd.getOccurredAt(), atRangeStart.getOccurredAt());
    }

    private List<AccountingEntry> findByCriteria(
            Optional<AccountingEventType> eventType,
            Optional<LocalDate> from,
            Optional<LocalDate> to) {
        return new com.pipelinepro.adapter.out.persistence.impl.JpaAccountingEntryRepository(
                springDataAccountingEntryRepository,
                accountingEntryEntityMapper).findByCriteria(eventType, from, to);
    }

    private static AccountingEntryEntity entityOf(AccountingEventType eventType, SourceAggregateType sourceAggregateType, Instant occurredAt) {
        AccountingEntryEntity entity = new AccountingEntryEntity();
        entity.setEventType(eventType);
        entity.setSourceAggregateType(sourceAggregateType);
        entity.setSourceAggregateId(UUID.randomUUID());
        entity.setAmount(new BigDecimal("15.00"));
        entity.setCurrency("EUR");
        entity.setOccurredAt(occurredAt);
        entity.setCreatedAt(occurredAt.plusSeconds(1));
        return entity;
    }
}
