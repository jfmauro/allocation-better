package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.mapper.AccountingEntryWebMapper;
import com.pipelinepro.adapter.in.web.v1.dto.request.AccountingEventTypeRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.AccountingEntryResponse;
import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.port.in.AccountingEntryQueryUseCase;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounting-entries")
public class AccountingEntryController {

    private static final Logger log = LoggerFactory.getLogger(AccountingEntryController.class);

    private final AccountingEntryQueryUseCase accountingEntryQueryUseCase;
    private final AccountingEntryWebMapper accountingEntryWebMapper;

    public AccountingEntryController(
            AccountingEntryQueryUseCase accountingEntryQueryUseCase,
            AccountingEntryWebMapper accountingEntryWebMapper) {
        this.accountingEntryQueryUseCase = accountingEntryQueryUseCase;
        this.accountingEntryWebMapper = accountingEntryWebMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTING_READ')")
    public List<AccountingEntryResponse> listAccountingEntries(
            @RequestParam(name = "eventType", required = false) AccountingEventTypeRequest eventType,
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("+++start listAccountingEntries+++");
        try {
            List<AccountingEntry> accountingEntries = accountingEntryQueryUseCase.listAccountingEntries(
                    Optional.ofNullable(eventType).map(AccountingEventTypeRequest::toDomain),
                    Optional.ofNullable(fromDate),
                    Optional.ofNullable(toDate));
            return accountingEntries.stream()
                    .map(accountingEntryWebMapper::toAccountingEntryResponse)
                    .toList();
        } finally {
            log.info("+++end listAccountingEntries+++");
        }
    }
}
