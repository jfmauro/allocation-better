package com.pipelinepro.application;

import com.pipelinepro.application.error.ResourceNotFoundException;
import com.pipelinepro.application.error.DuplicateResourceException;
import com.pipelinepro.application.error.ReferencedResourceNotFoundException;
import com.pipelinepro.application.port.out.DebtIntakeWorker;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.port.in.CreateDebtIntakeUseCase;
import com.pipelinepro.domain.port.in.command.CreateDebtCommand;
import com.pipelinepro.domain.port.out.IntakeAggregateType;
import com.pipelinepro.domain.port.out.IntakeAuditEventGateway;
import com.pipelinepro.domain.port.out.IntakeAuditLifecycle;
import com.pipelinepro.domain.port.out.command.PublishIntakeAuditEventCommand;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreateDebtIntakeApplicationService implements CreateDebtIntakeUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateDebtIntakeApplicationService.class);

    private final DebtIntakeWorker debtIntakeWorker;
    private final IntakeAuditEventGateway intakeAuditEventGateway;

    public CreateDebtIntakeApplicationService(
            DebtIntakeWorker debtIntakeWorker,
            IntakeAuditEventGateway intakeAuditEventGateway) {
        this.debtIntakeWorker = debtIntakeWorker;
        this.intakeAuditEventGateway = intakeAuditEventGateway;
    }

    @Override
    public Debt createDebt(CreateDebtCommand command) {
        log.info("+++start createDebt+++");
        UUID requestAggregateId = UUID.randomUUID();
        String correlationId = correlationId(command);
        try {
            validateCommand(command);
            requestAggregateId = requestAggregateId(command.idempotencyKey());
            publish(IntakeAggregateType.DEBT, requestAggregateId, IntakeAuditLifecycle.REQUESTED, null, correlationId);
            Debt savedDebt = debtIntakeWorker.createDebt(command);
            publish(IntakeAggregateType.DEBT, savedDebt.id(), IntakeAuditLifecycle.CREATED, null, correlationId);
            return savedDebt;
        } catch (RuntimeException exception) {
            if (exception instanceof ReferencedResourceNotFoundException referencedResourceNotFoundException) {
                publish(IntakeAggregateType.DEBT, requestAggregateId, IntakeAuditLifecycle.REJECTED, "NOT_FOUND", correlationId);
                throw new ResourceNotFoundException("Referenced resource not found", referencedResourceNotFoundException);
            }
            publish(IntakeAggregateType.DEBT, requestAggregateId, IntakeAuditLifecycle.REJECTED, reasonCode(exception), correlationId);
            throw exception;
        } finally {
            log.info("+++end createDebt+++");
        }
    }

    private UUID requestAggregateId(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must be non-blank");
        }
        return UUID.nameUUIDFromBytes(idempotencyKey.getBytes(StandardCharsets.UTF_8));
    }

    private void validateCommand(CreateDebtCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must be non-null");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must be non-blank");
        }
    }

    private String correlationId(CreateDebtCommand command) {
        return command == null ? "UNKNOWN" : command.correlationId();
    }

    private String reasonCode(RuntimeException exception) {
        if (exception instanceof IllegalArgumentException) {
            return "VALIDATION";
        }
        if (exception instanceof DuplicateResourceException) {
            return "DUPLICATE";
        }
        return "TECHNICAL";
    }

    private void publish(
            IntakeAggregateType aggregateType,
            UUID aggregateId,
            IntakeAuditLifecycle lifecycle,
            String reasonCode,
            String correlationId) {
        intakeAuditEventGateway.publish(new PublishIntakeAuditEventCommand(
                UUID.randomUUID(),
                aggregateType,
                aggregateId,
                lifecycle,
                reasonCode,
                correlationId,
                Instant.now()));
    }

}
