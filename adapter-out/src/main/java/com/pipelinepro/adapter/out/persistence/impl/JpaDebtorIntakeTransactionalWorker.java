package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.entity.IntakeRequestEntity;
import com.pipelinepro.adapter.out.persistence.entity.DebtorEntity;
import com.pipelinepro.adapter.out.persistence.mapper.DebtorEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataIntakeRequestRepository;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.DebtorType;
import jakarta.persistence.EntityManager;
import java.time.Instant;
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
public class JpaDebtorIntakeTransactionalWorker {

    private static final Logger log = LoggerFactory.getLogger(JpaDebtorIntakeTransactionalWorker.class);

    private static final String OPERATION_CREATE_DEBTOR = "CREATE_DEBTOR";
    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final ConcurrentMap<String, ReentrantLock> IDEMPOTENCY_LOCKS = new ConcurrentHashMap<>();

    private final SpringDataIntakeRequestRepository springDataIntakeRequestRepository;
    private final SpringDataDebtorRepository springDataDebtorRepository;
    private final DebtorEntityMapper debtorEntityMapper;
    private final EntityManager entityManager;

    public JpaDebtorIntakeTransactionalWorker(
            SpringDataIntakeRequestRepository springDataIntakeRequestRepository,
            SpringDataDebtorRepository springDataDebtorRepository,
            DebtorEntityMapper debtorEntityMapper,
            EntityManager entityManager) {
        this.springDataIntakeRequestRepository = springDataIntakeRequestRepository;
        this.springDataDebtorRepository = springDataDebtorRepository;
        this.debtorEntityMapper = debtorEntityMapper;
        this.entityManager = entityManager;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Debtor createDebtor(
            DebtorType debtorType,
            String displayName,
            String nationalNumber,
            String enterpriseNumber,
            String idempotencyKey,
            String correlationId) {
        log.info("+++start createDebtor+++");
        ReentrantLock idempotencyLock = IDEMPOTENCY_LOCKS.computeIfAbsent(idempotencyKey, ignored -> new ReentrantLock());
        idempotencyLock.lock();
        try {
            java.util.Optional<IntakeRequestEntity> existingIntakeRequest = springDataIntakeRequestRepository
                    .findByIdempotencyKeyForUpdate(idempotencyKey)
                    .map(this::assertDebtorOperation);

            if (existingIntakeRequest.isPresent()) {
                return replayDebtorFromExistingRequest(existingIntakeRequest.get());
            }

            return createNewDebtor(
                    debtorType,
                    displayName,
                    nationalNumber,
                    enterpriseNumber,
                    idempotencyKey,
                    correlationId);
        } finally {
            releaseLockAfterTransactionCompletion(idempotencyKey, idempotencyLock);
            log.info("+++end createDebtor+++");
        }
    }

    private Debtor createNewDebtor(
            DebtorType debtorType,
            String displayName,
            String nationalNumber,
            String enterpriseNumber,
            String idempotencyKey,
            String correlationId) {
        IntakeRequestEntity intakeRequest = createRequestedIntake(idempotencyKey, correlationId);
        if (!STATUS_REQUESTED.equals(intakeRequest.getStatus()) || intakeRequest.getResourceId() != null) {
            return replayDebtorFromExistingRequest(intakeRequest);
        }
        Instant now = Instant.now();

        DebtorEntity debtorEntity = createDebtorEntity(
                debtorType,
                displayName,
                nationalNumber,
                enterpriseNumber,
                now);
        var savedDebtor = saveDebtorWithConflictMapping(debtorEntity);

        intakeRequest.setResourceId(savedDebtor.getId());
        intakeRequest.setStatus(STATUS_CREATED);
        intakeRequest.setUpdatedAt(now);
        springDataIntakeRequestRepository.saveAndFlush(intakeRequest);
        return debtorEntityMapper.toDomain(savedDebtor);
    }

    private DebtorEntity createDebtorEntity(
            DebtorType debtorType,
            String displayName,
            String nationalNumber,
            String enterpriseNumber,
            Instant now) {
        DebtorEntity entity = new DebtorEntity();
        entity.setId(null);
        entity.setType(debtorType);
        entity.setDisplayName(displayName);
        entity.setNationalNumber(nationalNumber);
        entity.setEnterpriseNumber(enterpriseNumber);
        entity.setActive(true);
        entity.setCreatedAt(now);
        return entity;
    }

    private IntakeRequestEntity createRequestedIntake(String idempotencyKey, String correlationId) {
        IntakeRequestEntity entity = new IntakeRequestEntity();
        entity.setId(null);
        entity.setIdempotencyKey(idempotencyKey);
        entity.setOperation(OPERATION_CREATE_DEBTOR);
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
                    .map(this::assertDebtorOperation)
                    .orElseThrow(() -> duplicateKeyException);
        }
    }

    private DebtorEntity saveDebtorWithConflictMapping(DebtorEntity debtorEntity) {
        try {
            return springDataDebtorRepository.saveAndFlush(debtorEntity);
        } catch (DataIntegrityViolationException exception) {
            String message = extractMessage(exception).toLowerCase();
            if (message.contains("uk_debtor_national_number") || message.contains("national_number")) {
                throw new IllegalStateException("Duplicate debtor for national number", exception);
            }
            if (message.contains("uk_debtor_enterprise_number") || message.contains("enterprise_number")) {
                throw new IllegalStateException("Duplicate debtor for enterprise number", exception);
            }
            throw exception;
        }
    }

    private IntakeRequestEntity assertDebtorOperation(IntakeRequestEntity intakeRequestEntity) {
        if (!OPERATION_CREATE_DEBTOR.equals(intakeRequestEntity.getOperation())) {
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

    private java.util.Optional<Debtor> loadReplayedDebtor(IntakeRequestEntity intakeRequest) {
        if (!STATUS_CREATED.equals(intakeRequest.getStatus()) || intakeRequest.getResourceId() == null) {
            return java.util.Optional.empty();
        }
        return springDataDebtorRepository.findById(intakeRequest.getResourceId()).map(debtorEntityMapper::toDomain);
    }

    private Debtor replayDebtorFromExistingRequest(IntakeRequestEntity intakeRequest) {
        return loadReplayedDebtor(intakeRequest)
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
