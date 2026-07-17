package com.pipelinepro.application;

import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.SourceAggregateType;
import com.pipelinepro.domain.port.out.AccountingEntryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountingEntryApplicationServiceTest {

    @Test
    void should_create_and_append_accounting_entry() {
        AccountingEntryRepository repository = mock(AccountingEntryRepository.class);
        AccountingEntryApplicationService service = new AccountingEntryApplicationService(repository);
        UUID sourceAggregateId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-07-15T10:00:00Z");

        when(repository.append(org.mockito.ArgumentMatchers.any(AccountingEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountingEntry result = service.appendAccountingEntry(
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                sourceAggregateId,
                new BigDecimal("125.00"),
                "eur",
                occurredAt);

        ArgumentCaptor<AccountingEntry> captor = ArgumentCaptor.forClass(AccountingEntry.class);
        verify(repository).append(captor.capture());

        assertThat(result).isEqualTo(captor.getValue());
        assertThat(result.id()).isNotNull();
        assertThat(result.eventType()).isEqualTo(AccountingEventType.PAYMENT_ARRIVAL);
        assertThat(result.sourceAggregateType()).isEqualTo(SourceAggregateType.PAYMENT);
        assertThat(result.sourceAggregateId()).isEqualTo(sourceAggregateId);
        assertThat(result.amount()).isEqualByComparingTo("125.00");
        assertThat(result.currency()).isEqualTo("EUR");
        assertThat(result.occurredAt()).isEqualTo(occurredAt);
        assertThat(result.createdAt()).isEqualTo(occurredAt);
    }

    @Test
    void should_append_existing_accounting_entry() {
        AccountingEntryRepository repository = mock(AccountingEntryRepository.class);
        AccountingEntryApplicationService service = new AccountingEntryApplicationService(repository);
        AccountingEntry entry = AccountingEntry.append(
                UUID.randomUUID(),
                AccountingEventType.DEBT_ARRIVAL,
                SourceAggregateType.DEBT,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "EUR",
                Instant.parse("2026-07-15T11:00:00Z"),
                Instant.parse("2026-07-15T11:00:01Z"));
        when(repository.append(entry)).thenReturn(entry);

        assertThat(service.append(entry)).isSameAs(entry);

        verify(repository).append(entry);
    }

    @Test
    void should_propagate_repository_append_failure() {
        AccountingEntryRepository repository = mock(AccountingEntryRepository.class);
        AccountingEntryApplicationService service = new AccountingEntryApplicationService(repository);
        AccountingEntry entry = AccountingEntry.append(
                UUID.randomUUID(),
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "EUR",
                Instant.parse("2026-07-15T11:00:00Z"),
                Instant.parse("2026-07-15T11:00:00Z"));
        RuntimeException failure = new IllegalStateException("accounting unavailable");
        when(repository.append(entry)).thenThrow(failure);

        assertThatThrownBy(() -> service.append(entry)).isSameAs(failure);

        verify(repository).append(entry);
        verifyNoMoreInteractions(repository);
    }
}
