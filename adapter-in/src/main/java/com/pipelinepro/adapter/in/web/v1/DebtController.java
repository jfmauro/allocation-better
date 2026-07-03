package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.mapper.DebtWebMapper;
import com.pipelinepro.adapter.in.web.error.BadRequestWebException;
import com.pipelinepro.adapter.in.web.error.NotFoundWebException;
import com.pipelinepro.adapter.in.web.v1.dto.request.CreateDebtRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.DebtListResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.DebtResponse;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.port.in.CreateDebtIntakeUseCase;
import com.pipelinepro.domain.port.in.QueryDebtUseCase;
import com.pipelinepro.domain.port.in.command.CreateDebtCommand;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class DebtController {

    private static final Logger log = LoggerFactory.getLogger(DebtController.class);

    private final QueryDebtUseCase queryDebtUseCase;
    private final CreateDebtIntakeUseCase createDebtIntakeUseCase;
    private final DebtWebMapper debtWebMapper;

    public DebtController(QueryDebtUseCase queryDebtUseCase, CreateDebtIntakeUseCase createDebtIntakeUseCase, DebtWebMapper debtWebMapper) {
        this.queryDebtUseCase = queryDebtUseCase;
        this.createDebtIntakeUseCase = createDebtIntakeUseCase;
        this.debtWebMapper = debtWebMapper;
    }

    @PostMapping("/debts")
    @PreAuthorize("hasAuthority('CREATE_DEBT')")
    public ResponseEntity<DebtResponse> createDebt(
            @Valid @RequestBody CreateDebtRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(name = "X-Correlation-Id", required = false) String correlationId) {
        log.info("+++start createDebt+++");
        try {
            requireHeader(idempotencyKey, "Idempotency-Key");
            requireHeader(correlationId, "X-Correlation-Id");
            CreateDebtCommand command = debtWebMapper.toCreateDebtCommand(request, idempotencyKey.trim(), correlationId.trim());
            Debt debt = createDebtIntakeUseCase.createDebt(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(debtWebMapper.toDebtResponse(debt));
        } finally {
            log.info("+++end createDebt+++");
        }
    }

    @GetMapping("/debtors/{debtorId}/debts")
    public ResponseEntity<DebtListResponse> listDebtorDebts(
            @PathVariable UUID debtorId,
            @RequestParam(name = "status", required = false) List<DebtStatus> statuses) {
        log.info("+++start listDebtorDebts+++");
        try {
            List<Debt> debts = queryDebtUseCase.listDebtsByDebtor(debtorId, statuses);
            if (debts.isEmpty()) {
                throw new NotFoundWebException("Debts not found for debtor: " + debtorId);
            }
            return ResponseEntity.ok(debtWebMapper.toDebtListResponse(debtorId, debts));
        } finally {
            log.info("+++end listDebtorDebts+++");
        }
    }

    @GetMapping("/debts/{debtId}")
    public ResponseEntity<DebtResponse> getDebt(@PathVariable UUID debtId) {
        log.info("+++start getDebt+++");
        try {
            Debt debt = queryDebtUseCase.getDebt(debtId)
                    .orElseThrow(() -> new NotFoundWebException("Debt not found: " + debtId));
            return ResponseEntity.ok(debtWebMapper.toDebtResponse(debt));
        } finally {
            log.info("+++end getDebt+++");
        }
    }

    private static void requireHeader(String value, String headerName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestWebException(headerName + " is required");
        }
    }
}
