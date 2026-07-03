package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.entity.IntakeRequestEntity;
import com.pipelinepro.adapter.out.persistence.entity.DebtEntity;
import com.pipelinepro.adapter.out.persistence.mapper.DebtEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataIntakeRequestRepository;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Repository
public class JpaDebtIntakeTransactionalWorker {

    private static final Logger log = LoggerFactory.getLogger(JpaDebtIntakeTransactionalWorker.class);

    private static final String OPERATION_CREATE_DEBT = "CREATE_DEBT";
    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final ConcurrentMap<String, ReentrantLock> IDEMPOTENCY_LOCKS = new ConcurrentHashMap<>();

    private final SpringDataIntakeRequestRepository springDataIntakeRequestRepository;
    private final SpringDataDebtRepository springDataDebtRepository;
    private final SpringDataDebtorRepository springDataDebtorRepository;
    private final DebtEntityMapper debtEntityMapper;
    private final EntityManager entityManager;

    public JpaDebtIntakeTransactionalWorker(
            SpringDataIntakeRequestRepository springDataIntakeRequestRepository,
            SpringDataDebtRepository springDataDebtRepository,
            SpringDataDebtorRepository springDataDebtorRepository,
            DebtEntityMapper debtEntityMapper,
            EntityManager entityManager) {
        this.springDataIntakeRequestRepository = springDataIntakeRequestRepository;
        this.springDataDebtRepository = springDataDebtRepository;
        this.springDataDebtorRepository = springDataDebtorRepository;
        this.debtEntityMapper = debtEntityMapper;
        this.entityManager = entityManager;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Debt createDebt(
            UUID debtorId,
            String reference,
            java.math.BigDecimal originalAmount,
            String currency,
            DebtStatus openingStatus,
            java.time.LocalDate dueDate,
            String idempotencyKey,
            String correlationId) {
        log.info("+++start createDebt+++");
        ReentrantLock idempotencyLock = IDEMPOTENCY_LOCKS.computeIfAbsent(idempotencyKey, ignored -> new ReentrantLock());
        idempotencyLock.lock();
        try {
            Optional<IntakeRequestEntity> existingIntakeRequest = springDataIntakeRequestRepository
                    .findByIdempotencyKeyForUpdate(idempotencyKey)
                    .map(this::assertDebtOperation);

            if (existingIntakeRequest.isPresent()) {
                return replayDebtFromExistingRequest(existingIntakeRequest.get());
            }

            return createNewDebt(
                    debtorId,
                    reference,
                    originalAmount,
                    currency,
                    openingStatus,
                    dueDate,
                    idempotencyKey,
                    correlationId);
        } finally {
            releaseLockAfterTransactionCompletion(idempotencyKey, idempotencyLock);
            log.info("+++end createDebt+++");
        }
    }

    private Debt createNewDebt(
            UUID debtorId,
            String reference,
            java.math.BigDecimal originalAmount,
            String currency,
            DebtStatus openingStatus,
            java.time.LocalDate dueDate,
            String idempotencyKey,
            String correlationId) {
        IntakeRequestEntity intakeRequest = createRequestedIntake(idempotencyKey, correlationId);
        if (!STATUS_REQUESTED.equals(intakeRequest.getStatus()) || intakeRequest.getResourceId() != null) {
            return replayDebtFromExistingRequest(intakeRequest);
        }
        if (!springDataDebtorRepository.existsById(debtorId)) {
            throw new IllegalStateException("Debtor not found: " + debtorId);
        }
        Instant now = Instant.now();

        DebtEntity debtEntity = createDebtEntity(debtorId, reference, originalAmount, currency, openingStatus, dueDate, now);
        var savedDebt = saveDebtWithConflictMapping(debtEntity);

        intakeRequest.setResourceId(savedDebt.getId());
        intakeRequest.setStatus(STATUS_CREATED);
        intakeRequest.setUpdatedAt(now);
        springDataIntakeRequestRepository.saveAndFlush(intakeRequest);
        return debtEntityMapper.toDomain(savedDebt);
    }

    private DebtEntity createDebtEntity(
            UUID debtorId,
            String reference,
            java.math.BigDecimal originalAmount,
            String currency,
            DebtStatus openingStatus,
            java.time.LocalDate dueDate,
            Instant now) {
        DebtEntity entity = new DebtEntity();
        entity.setId(null);
        entity.setDebtorId(debtorId);
        entity.setReference(reference);
        entity.setOriginalAmount(originalAmount);
        entity.setRemainingAmount(originalAmount);
        entity.setCurrency(currency);
        entity.setStatus(openingStatus);
        entity.setDueDate(dueDate);
        entity.setVersion(0L);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private IntakeRequestEntity createRequestedIntake(String idempotencyKey, String correlationId) {
        IntakeRequestEntity entity = new IntakeRequestEntity();
        entity.setId(null);
        entity.setIdempotencyKey(idempotencyKey);
        entity.setOperation(OPERATION_CREATE_DEBT);
        entity.setCorrelationId(correlationId);
        entity.setStatus(STATUS_REQUESTED);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
        try {
            return springDataIntakeRequestRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException duplicateKeyException) {
            entityManager.clear();
            return springDataIntakeRequestRepository
                    .findByIdempotencyKeyForUpdate(idempotencyKey)
                    .map(this::assertDebtOperation)
                    .orElseThrow(() -> duplicateKeyException);
        }
    }

    private DebtEntity saveDebtWithConflictMapping(DebtEntity debtEntity) {
        try {
            return springDataDebtRepository.saveAndFlush(debtEntity);
        } catch (DataIntegrityViolationException exception) {
            String message = extractMessage(exception).toLowerCase();
            if (message.contains("uk_debt_reference_global") || message.contains("reference")) {
                throw new IllegalStateException("Duplicate debt reference: " + debtEntity.getReference(), exception);
            }
            throw exception;
        }
    }

    private IntakeRequestEntity assertDebtOperation(IntakeRequestEntity intakeRequestEntity) {
        if (!OPERATION_CREATE_DEBT.equals(intakeRequestEntity.getOperation())) {
            throw new IllegalStateException("Duplicate idempotency key used for another operation");
        }
        return intakeRequestEntity;
    }

    private String extractMessage(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable cursor = throwable;
        while (cursor != null) {
            if (cursor.getMessage() != null) {
                builder.append(cursor.getMessage()).append(' ');
            }
            cursor = cursor.getCause();
        }
        return builder.toString();
    }

    private Optional<Debt> loadReplayedDebt(IntakeRequestEntity intakeRequest) {
        if (!STATUS_CREATED.equals(intakeRequest.getStatus()) || intakeRequest.getResourceId() == null) {
            return Optional.empty();
        }
        return springDataDebtRepository.findById(intakeRequest.getResourceId()).map(debtEntityMapper::toDomain);
    }

    private Debt replayDebtFromExistingRequest(IntakeRequestEntity intakeRequest) {
        return loadReplayedDebt(intakeRequest)
                .orElseThrow(() -> new IllegalStateException("Idempotency key already exists but replay resource is not available"));
    }

    private void releaseLockAfterTransactionCompletion(String idempotencyKey, ReentrantLock idempotencyLock) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    releaseLock(idempotencyKey, idempotencyLock);
                }
            });
            return;
        }
        releaseLock(idempotencyKey, idempotencyLock);
    }

    private void releaseLock(String idempotencyKey, ReentrantLock idempotencyLock) {
        idempotencyLock.unlock();
        if (!idempotencyLock.hasQueuedThreads()) {
            IDEMPOTENCY_LOCKS.remove(idempotencyKey, idempotencyLock);
        }
    }
}
