package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.SecurityConfig;
import com.pipelinepro.adapter.in.web.error.GlobalRestExceptionHandler;
import com.pipelinepro.adapter.in.web.mapper.AccountingEntryWebMapper;
import com.pipelinepro.adapter.in.web.v1.dto.response.AccountingEntryResponse;
import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.SourceAggregateType;
import com.pipelinepro.domain.port.in.AccountingEntryQueryUseCase;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountingEntryController.class)
@Import({GlobalRestExceptionHandler.class, SecurityConfig.class})
class AccountingEntryControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountingEntryQueryUseCase accountingEntryQueryUseCase;

    @MockitoBean
    private AccountingEntryWebMapper accountingEntryWebMapper;

    @Test
    @WithMockUser(authorities = "ACCOUNTING_READ")
    void listAccountingEntries_shouldReturn200AndDelegateFiltersAndOrdering_whenRequestIsValid() throws Exception {
        AccountingEntry newest = AccountingEntry.append(
                UUID.randomUUID(),
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                "EUR",
                Instant.parse("2026-07-10T10:00:00Z"),
                Instant.parse("2026-07-10T10:00:00Z"));
        AccountingEntry older = AccountingEntry.append(
                UUID.randomUUID(),
                AccountingEventType.PAYMENT_ARRIVAL,
                SourceAggregateType.PAYMENT,
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "EUR",
                Instant.parse("2026-07-09T10:00:00Z"),
                Instant.parse("2026-07-09T10:00:00Z"));
        when(accountingEntryQueryUseCase.listAccountingEntries(
                Optional.of(AccountingEventType.PAYMENT_ARRIVAL),
                Optional.of(LocalDate.parse("2026-07-01")),
                Optional.of(LocalDate.parse("2026-07-31"))))
                .thenReturn(List.of(newest, older));
        when(accountingEntryWebMapper.toAccountingEntryResponse(newest)).thenReturn(new AccountingEntryResponse(
                "PAYMENT_ARRIVAL",
                "PAYMENT",
                newest.sourceAggregateId(),
                newest.amount(),
                newest.currency(),
                newest.occurredAt(),
                newest.createdAt()));
        when(accountingEntryWebMapper.toAccountingEntryResponse(older)).thenReturn(new AccountingEntryResponse(
                "PAYMENT_ARRIVAL",
                "PAYMENT",
                older.sourceAggregateId(),
                older.amount(),
                older.currency(),
                older.occurredAt(),
                older.createdAt()));

        mockMvc.perform(get("/accounting-entries")
                        .queryParam("eventType", "PAYMENT_ARRIVAL")
                        .queryParam("fromDate", "2026-07-01")
                        .queryParam("toDate", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sourceAggregateId").value(newest.sourceAggregateId().toString()))
                .andExpect(jsonPath("$[1].sourceAggregateId").value(older.sourceAggregateId().toString()));

        verify(accountingEntryQueryUseCase).listAccountingEntries(
                Optional.of(AccountingEventType.PAYMENT_ARRIVAL),
                Optional.of(LocalDate.parse("2026-07-01")),
                Optional.of(LocalDate.parse("2026-07-31")));
    }

    @Test
    @WithMockUser(authorities = "ACCOUNTING_READ")
    void listAccountingEntries_shouldReturn200WithEmptyArray_whenNoEntriesFound() throws Exception {
        when(accountingEntryQueryUseCase.listAccountingEntries(Optional.empty(), Optional.empty(), Optional.empty()))
                .thenReturn(List.of());

        mockMvc.perform(get("/accounting-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(accountingEntryQueryUseCase).listAccountingEntries(Optional.empty(), Optional.empty(), Optional.empty());
        verifyNoInteractions(accountingEntryWebMapper);
    }

    @Test
    @WithMockUser(authorities = "ACCOUNTING_READ")
    void listAccountingEntries_shouldReturn400_whenEventTypeIsMalformed() throws Exception {
        mockMvc.perform(get("/accounting-entries").queryParam("eventType", "NOT_AN_EVENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(accountingEntryQueryUseCase, accountingEntryWebMapper);
    }

    @Test
    @WithMockUser(authorities = "ACCOUNTING_READ")
    void listAccountingEntries_shouldReturn400_whenDateIsMalformed() throws Exception {
        mockMvc.perform(get("/accounting-entries").queryParam("fromDate", "2026-31-12"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(accountingEntryQueryUseCase, accountingEntryWebMapper);
    }

    @Test
    @WithMockUser(authorities = "ACCOUNTING_READ")
    void listAccountingEntries_shouldReturn400_whenFromDateIsAfterToDate_businessRuleComesFromApplication() throws Exception {
        when(accountingEntryQueryUseCase.listAccountingEntries(
                Optional.empty(),
                Optional.of(LocalDate.parse("2026-07-31")),
                Optional.of(LocalDate.parse("2026-07-01"))))
                .thenThrow(new IllegalArgumentException("fromDate must be less than or equal to toDate"));

        mockMvc.perform(get("/accounting-entries")
                        .queryParam("fromDate", "2026-07-31")
                        .queryParam("toDate", "2026-07-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(accountingEntryQueryUseCase).listAccountingEntries(
                Optional.empty(),
                Optional.of(LocalDate.parse("2026-07-31")),
                Optional.of(LocalDate.parse("2026-07-01")));
        verifyNoInteractions(accountingEntryWebMapper);
    }

    @Test
    @WithMockUser(authorities = "VIEW_DEBT_MASTER_DATA")
    void listAccountingEntries_shouldReturn403_whenAuthorityIsMissing() throws Exception {
        mockMvc.perform(get("/accounting-entries"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(accountingEntryQueryUseCase, accountingEntryWebMapper);
    }
}
