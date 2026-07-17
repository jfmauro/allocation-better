package com.pipelinepro.application;

import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.SourceAggregateType;
import com.pipelinepro.domain.port.out.AccountingEntryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountingEntryQueryApplicationServiceTest {

    @Test
    void should_delegate_accounting_entry_query_to_repository() {
        AccountingEntryRepository repository = mock(AccountingEntryRepository.class);
        AccountingEntryQueryApplicationService service = new AccountingEntryQueryApplicationService(repository);
        Optional<AccountingEventType> eventType = Optional.of(AccountingEventType.PAYMENT_ARRIVAL);
        Optional<LocalDate> fromDate = Optional.of(LocalDate.of(2026, 7, 1));
        Optional<LocalDate> toDate = Optional.of(LocalDate.of(2026, 7, 15));
        AccountingEntry entry = AccountingEntry.append(
                UUID.randomUUID(),
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "EUR",
                Instant.parse("2026-07-10T10:00:00Z"),
                Instant.parse("2026-07-10T10:00:01Z"));
        when(repository.findByCriteria(eventType, fromDate, toDate)).thenReturn(List.of(entry));

        assertThat(service.listAccountingEntries(eventType, fromDate, toDate)).containsExactly(entry);

        verify(repository).findByCriteria(eventType, fromDate, toDate);
    }

    @Test
    void should_reject_an_invalid_date_range_without_querying_repository() {
        AccountingEntryRepository repository = mock(AccountingEntryRepository.class);
        AccountingEntryQueryApplicationService service = new AccountingEntryQueryApplicationService(repository);
        Optional<LocalDate> fromDate = Optional.of(LocalDate.of(2026, 7, 16));
        Optional<LocalDate> toDate = Optional.of(LocalDate.of(2026, 7, 15));

        assertThatThrownBy(() -> service.listAccountingEntries(Optional.empty(), fromDate, toDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fromDate must be less than or equal to toDate");

        verify(repository, never()).findByCriteria(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }
}
