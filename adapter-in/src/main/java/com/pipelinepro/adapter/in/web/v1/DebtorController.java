package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.error.BadRequestWebException;
import com.pipelinepro.adapter.in.web.mapper.DebtorWebMapper;
import com.pipelinepro.adapter.in.web.v1.dto.request.CreateDebtorRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.DebtorResponse;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.DebtorType;
import com.pipelinepro.domain.port.in.CreateDebtorIntakeUseCase;
import com.pipelinepro.domain.port.in.QueryDebtorUseCase;
import com.pipelinepro.domain.port.in.command.CreateDebtorCommand;
import com.pipelinepro.domain.port.in.command.DebtorSearchCriteria;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class DebtorController {

    private static final Logger log = LoggerFactory.getLogger(DebtorController.class);
    private final CreateDebtorIntakeUseCase createDebtorIntakeUseCase;
    private final QueryDebtorUseCase queryDebtorUseCase;
    private final DebtorWebMapper debtorWebMapper;

    public DebtorController(CreateDebtorIntakeUseCase createDebtorIntakeUseCase, QueryDebtorUseCase queryDebtorUseCase, DebtorWebMapper debtorWebMapper) {
        this.createDebtorIntakeUseCase = createDebtorIntakeUseCase;
        this.queryDebtorUseCase = queryDebtorUseCase;
        this.debtorWebMapper = debtorWebMapper;
    }

    @PostMapping("/debtors")
    @PreAuthorize("hasAuthority('CREATE_DEBTOR')")
    public ResponseEntity<DebtorResponse> createDebtor(
            @Valid @RequestBody CreateDebtorRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(name = "X-Correlation-Id", required = false) String correlationId) {
        log.info("+++start createDebtor+++");
        try {
            requireHeader(idempotencyKey, "Idempotency-Key");
            requireHeader(correlationId, "X-Correlation-Id");
            CreateDebtorCommand command = debtorWebMapper.toCreateDebtorCommand(request, idempotencyKey.trim(), correlationId.trim());
            Debtor debtor = createDebtorIntakeUseCase.createDebtor(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(debtorWebMapper.toDebtorResponse(debtor));
        } finally {
            log.info("+++end createDebtor+++");
        }
    }

    @GetMapping("/debtors")
    @PreAuthorize("hasAuthority('VIEW_DEBTOR_MASTER_DATA')")
    public ResponseEntity<java.util.List<DebtorResponse>> listDebtors(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "debtorType", required = false) DebtorType debtorType,
            @RequestParam(name = "active", required = false) Boolean active) {
        log.info("+++start listDebtors+++");
        try {
            java.util.List<DebtorResponse> responses = queryDebtorUseCase.listDebtors(
                    new DebtorSearchCriteria(query, debtorType, active)).stream()
                    .map(debtorWebMapper::toDebtorResponse)
                    .toList();
            return ResponseEntity.ok(responses);
        } finally {
            log.info("+++end listDebtors+++");
        }
    }

    private static void requireHeader(String value, String headerName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestWebException(headerName + " is required");
        }
    }
}
