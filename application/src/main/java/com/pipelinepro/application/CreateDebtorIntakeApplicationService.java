package com.pipelinepro.application;

import com.pipelinepro.domain.Debtor;
import com.pipelinepro.application.error.DuplicateResourceException;
import com.pipelinepro.application.port.out.DebtorIntakeWorker;
import com.pipelinepro.domain.port.in.CreateDebtorIntakeUseCase;
import com.pipelinepro.domain.port.in.command.CreateDebtorCommand;
import com.pipelinepro.domain.port.out.IntakeAggregateType;
import com.pipelinepro.domain.port.out.IntakeAuditEventGateway;
import com.pipelinepro.domain.port.out.IntakeAuditLifecycle;
import com.pipelinepro.domain.port.out.command.PublishIntakeAuditEventCommand;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreateDebtorIntakeApplicationService implements CreateDebtorIntakeUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateDebtorIntakeApplicationService.class);

    private final DebtorIntakeWorker debtorIntakeWorker;
    private final IntakeAuditEventGateway intakeAuditEventGateway;

    public CreateDebtorIntakeApplicationService(
            DebtorIntakeWorker debtorIntakeWorker,
            IntakeAuditEventGateway intakeAuditEventGateway) {
        this.debtorIntakeWorker = debtorIntakeWorker;
        this.intakeAuditEventGateway = intakeAuditEventGateway;
    }

    @Override
    public Debtor createDebtor(CreateDebtorCommand command) {
        log.info("+++start createDebtor+++");
        UUID requestAggregateId = UUID.randomUUID();
        String correlationId = correlationId(command);
        try {
            validateCommand(command);
            requestAggregateId = requestAggregateId(command.idempotencyKey());
            publish(IntakeAggregateType.DEBTOR, requestAggregateId, IntakeAuditLifecycle.REQUESTED, null, correlationId);
            Debtor savedDebtor = debtorIntakeWorker.createDebtor(command);
            publish(IntakeAggregateType.DEBTOR, savedDebtor.id(), IntakeAuditLifecycle.CREATED, null, correlationId);
            return savedDebtor;
        } catch (RuntimeException exception) {
            publish(IntakeAggregateType.DEBTOR, requestAggregateId, IntakeAuditLifecycle.REJECTED, reasonCode(exception), correlationId);
            throw exception;
        } finally {
            log.info("+++end createDebtor+++");
        }
    }

    private UUID requestAggregateId(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must be non-blank");
        }
        return UUID.nameUUIDFromBytes(idempotencyKey.getBytes(StandardCharsets.UTF_8));
    }

    private void validateCommand(CreateDebtorCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must be non-null");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must be non-blank");
        }
    }

    private String correlationId(CreateDebtorCommand command) {
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
