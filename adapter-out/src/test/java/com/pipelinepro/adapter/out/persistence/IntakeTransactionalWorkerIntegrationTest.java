package com.pipelinepro.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pipelinepro.adapter.out.persistence.impl.JpaDebtorIntakeTransactionalWorker;
import com.pipelinepro.adapter.out.persistence.impl.JpaDebtIntakeTransactionalWorker;
import com.pipelinepro.adapter.out.persistence.mapper.AccountingEntryEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtorEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAccountingEntryRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataIntakeRequestRepository;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.DebtorType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TestJpaConfiguration.class)
@Import(IntakeTransactionalWorkerIntegrationTest.IntakeWorkerTestConfiguration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class IntakeTransactionalWorkerIntegrationTest {

    private static final int CONCURRENT_REQUEST_COUNT = 8;
    private static final AtomicBoolean FAIL_ACCOUNTING_WRITE = new AtomicBoolean(false);

    @Autowired
    private JpaDebtorIntakeTransactionalWorker debtorWorker;

    @Autowired
    private JpaDebtIntakeTransactionalWorker debtWorker;

    @Autowired
    private SpringDataIntakeRequestRepository springDataIntakeRequestRepository;

    @Autowired
    private SpringDataDebtorRepository springDataDebtorRepository;

    @Autowired
    private SpringDataDebtRepository springDataDebtRepository;

    @Autowired
    private SpringDataAccountingEntryRepository springDataAccountingEntryRepository;

    @AfterEach
    void cleanDatabase() {
        springDataDebtRepository.deleteAll();
        springDataAccountingEntryRepository.deleteAll();
        springDataDebtorRepository.deleteAll();
        springDataIntakeRequestRepository.deleteAll();
    }

    @Test
    void should_create_single_debtor_for_replayed_idempotency_key() {
        var first = debtorWorker.createDebtor(
                DebtorType.NATURAL_PERSON,
                "Replay Debtor",
                "85073003328",
                null,
                "idem-debtor-1",
                "corr-debtor-1");

        var replay = debtorWorker.createDebtor(
                DebtorType.NATURAL_PERSON,
                "Replay Debtor",
                "85073003328",
                null,
                "idem-debtor-1",
                "corr-debtor-1");

        assertThat(first.id()).isEqualTo(replay.id());
        assertThat(springDataDebtorRepository.count()).isEqualTo(1L);
        assertThat(springDataIntakeRequestRepository.count()).isEqualTo(1L);
        assertThat(springDataIntakeRequestRepository.findByIdempotencyKey("idem-debtor-1"))
                .hasValueSatisfying(entity -> {
                    assertThat(entity.getOperation()).isEqualTo("CREATE_DEBTOR");
                    assertThat(entity.getStatus()).isEqualTo("CREATED");
                    assertThat(entity.getResourceId()).isEqualTo(first.id());
                });
    }

    @Test
    void should_create_single_debt_for_replayed_idempotency_key() {
        UUID debtorId = createDebtorId();
        var first = debtWorker.createDebt(
                debtorId,
                "DEBT-REPLAY-1",
                new BigDecimal("125.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-debt-1",
                "corr-debt-1");

        var replay = debtWorker.createDebt(
                debtorId,
                "DEBT-REPLAY-1",
                new BigDecimal("125.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-debt-1",
                "corr-debt-1");

        assertThat(first.id()).isEqualTo(replay.id());
        assertThat(springDataDebtRepository.count()).isEqualTo(1L);
        assertThat(springDataAccountingEntryRepository.count()).isEqualTo(1L);
        assertThat(springDataAccountingEntryRepository.findAll()).singleElement().satisfies(entry -> {
            assertThat(entry.getEventType().name()).isEqualTo("DEBT_ARRIVAL");
            assertThat(entry.getSourceAggregateId()).isEqualTo(first.id());
        });
        assertThat(springDataIntakeRequestRepository.findByIdempotencyKey("idem-debt-1"))
                .hasValueSatisfying(entity -> {
                    assertThat(entity.getOperation()).isEqualTo("CREATE_DEBT");
                    assertThat(entity.getStatus()).isEqualTo("CREATED");
                    assertThat(entity.getResourceId()).isEqualTo(first.id());
                });
    }

    @Test
    void should_reject_reused_idempotency_key_for_different_operation() {
        debtorWorker.createDebtor(
                DebtorType.NATURAL_PERSON,
                "Replay Debtor",
                "85073003328",
                null,
                "idem-cross-op-1",
                "corr-cross-op-1");

        UUID debtorId = createDebtorId();
        assertThatThrownBy(() -> debtWorker.createDebt(
                        debtorId,
                        "DEBT-CROSS-OP-1",
                        new BigDecimal("10.00"),
                        "EUR",
                        DebtStatus.OPEN,
                        null,
                        "idem-cross-op-1",
                        "corr-cross-op-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Duplicate idempotency key used for another operation");
    }

    @Test
    void should_throw_not_found_when_debtor_does_not_exist() {
        assertThatThrownBy(() -> debtWorker.createDebt(
                        UUID.randomUUID(),
                        "DEBT-NOT-FOUND-1",
                        new BigDecimal("20.00"),
                        "EUR",
                        DebtStatus.OPEN,
                        null,
                        "idem-not-found-1",
                        "corr-not-found-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Debtor not found");
    }

    @Test
    void should_map_duplicate_debt_reference_conflict() {
        UUID debtorId = createDebtorId();

        debtWorker.createDebt(
                debtorId,
                "DEBT-DUP-REF-1",
                new BigDecimal("40.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-dup-ref-1",
                "corr-dup-ref-1");

        assertThatThrownBy(() -> debtWorker.createDebt(
                        debtorId,
                        "DEBT-DUP-REF-1",
                        new BigDecimal("40.00"),
                        "EUR",
                        DebtStatus.OPEN,
                        null,
                        "idem-dup-ref-2",
                        "corr-dup-ref-2"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Duplicate debt reference");
    }

    @Test
    void should_persist_debt_arrival_accounting_entry_when_debt_is_created() {
        UUID debtorId = createDebtorId();

        var debt = debtWorker.createDebt(
                debtorId,
                "DEBT-ACCOUNTING-1",
                new BigDecimal("40.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-accounting-1",
                "corr-accounting-1");

        assertThat(springDataAccountingEntryRepository.count()).isEqualTo(1L);
        assertThat(springDataAccountingEntryRepository.findAll()).singleElement().satisfies(entry -> {
            assertThat(entry.getEventType().name()).isEqualTo("DEBT_ARRIVAL");
            assertThat(entry.getSourceAggregateType().name()).isEqualTo("DEBT");
            assertThat(entry.getSourceAggregateId()).isEqualTo(debt.id());
            assertThat(entry.getAmount()).isEqualByComparingTo("40.00");
            assertThat(entry.getCurrency()).isEqualTo("EUR");
        });
    }

    @Test
    void should_rollback_debt_creation_when_accounting_write_fails() {
        FAIL_ACCOUNTING_WRITE.set(true);

        try {
            UUID debtorId = createDebtorId();

            assertThatThrownBy(() -> debtWorker.createDebt(
                            debtorId,
                            "DEBT-ACCOUNTING-FAIL-1",
                            new BigDecimal("55.00"),
                            "EUR",
                            DebtStatus.OPEN,
                            null,
                            "idem-accounting-fail-1",
                            "corr-accounting-fail-1"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("accounting-write-failure");

            assertThat(springDataDebtRepository.count()).isZero();
            assertThat(springDataAccountingEntryRepository.count()).isZero();
            assertThat(springDataIntakeRequestRepository.findByIdempotencyKey("idem-accounting-fail-1")).isEmpty();
        } finally {
            FAIL_ACCOUNTING_WRITE.set(false);
        }
    }

    @Test
    void should_create_exactly_one_debtor_for_concurrent_same_idempotency_key() throws Exception {
        List<UUID> debtorIds = executeConcurrently(() -> debtorWorker.createDebtor(
                        DebtorType.NATURAL_PERSON,
                        "Concurrent Debtor",
                        "85073003328-concurrent",
                        null,
                        "idem-debtor-concurrent-1",
                        "corr-debtor-concurrent-1")
                .id());

        Set<UUID> distinctDebtorIds = new HashSet<>(debtorIds);
        assertThat(distinctDebtorIds).hasSize(1);
        UUID expectedDebtorId = distinctDebtorIds.iterator().next();

        assertThat(springDataDebtorRepository.count()).isEqualTo(1L);
        assertThat(springDataIntakeRequestRepository.findByIdempotencyKey("idem-debtor-concurrent-1"))
                .hasValueSatisfying(entity -> {
                    assertThat(entity.getStatus()).isEqualTo("CREATED");
                    assertThat(entity.getResourceId()).isEqualTo(expectedDebtorId);
                });
    }

    @Test
    void should_create_exactly_one_debt_for_concurrent_same_idempotency_key() throws Exception {
        UUID debtorId = createDebtorId();

        List<UUID> debtIds = executeConcurrently(() -> debtWorker.createDebt(
                        debtorId,
                        "DEBT-CONCURRENT-1",
                        new BigDecimal("210.00"),
                        "EUR",
                        DebtStatus.OPEN,
                        null,
                        "idem-debt-concurrent-1",
                        "corr-debt-concurrent-1")
                .id());

        Set<UUID> distinctDebtIds = new HashSet<>(debtIds);
        assertThat(distinctDebtIds).hasSize(1);
        UUID expectedDebtId = distinctDebtIds.iterator().next();

        assertThat(springDataDebtRepository.count()).isEqualTo(1L);
        assertThat(springDataIntakeRequestRepository.findByIdempotencyKey("idem-debt-concurrent-1"))
                .hasValueSatisfying(entity -> {
                    assertThat(entity.getStatus()).isEqualTo("CREATED");
                    assertThat(entity.getResourceId()).isEqualTo(expectedDebtId);
                });
    }

    private <T> List<T> executeConcurrently(Callable<T> callable) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUEST_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(CONCURRENT_REQUEST_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<T>> futures = new ArrayList<>(CONCURRENT_REQUEST_COUNT);
        try {
            for (int i = 0; i < CONCURRENT_REQUEST_COUNT; i++) {
                futures.add(executorService.submit(() -> {
                    readyLatch.countDown();
                    boolean started = startLatch.await(5, TimeUnit.SECONDS);
                    if (!started) {
                        throw new IllegalStateException("Concurrent test start latch timeout");
                    }
                    return callable.call();
                }));
            }

            boolean allReady = readyLatch.await(5, TimeUnit.SECONDS);
            if (!allReady) {
                throw new IllegalStateException("Concurrent test workers were not ready in time");
            }
            startLatch.countDown();

            List<T> results = new ArrayList<>(CONCURRENT_REQUEST_COUNT);
            for (Future<T> future : futures) {
                results.add(getFutureValue(future));
            }
            return results;
        } finally {
            executorService.shutdownNow();
        }
    }

    private <T> T getFutureValue(Future<T> future) throws Exception {
        try {
            return future.get(20, TimeUnit.SECONDS);
        } catch (ExecutionException executionException) {
            Throwable cause = executionException.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw new RuntimeException(cause);
        }
    }

    private UUID createDebtorId() {
        return debtorWorker.createDebtor(
                DebtorType.NATURAL_PERSON,
                "Debt Owner",
                "85073003328-owner",
                null,
                "idem-owner-1",
                "corr-owner-1").id();
    }

    @TestConfiguration
    static class IntakeWorkerTestConfiguration {

        @Bean
        AccountingEntryEntityMapper accountingEntryEntityMapper() {
            return Mappers.getMapper(AccountingEntryEntityMapper.class);
        }

        @Bean
        DebtorEntityMapper debtorEntityMapper() {
            return Mappers.getMapper(DebtorEntityMapper.class);
        }

        @Bean
        DebtEntityMapper debtEntityMapper() {
            return Mappers.getMapper(DebtEntityMapper.class);
        }

        @Bean
        AccountingEntrySaveFailureAspect accountingEntrySaveFailureAspect() {
            return new AccountingEntrySaveFailureAspect();
        }

        @Bean
        JpaDebtorIntakeTransactionalWorker jpaDebtorIntakeTransactionalWorker(
                SpringDataIntakeRequestRepository intakeRequestRepository,
                SpringDataDebtorRepository debtorRepository,
                DebtorEntityMapper debtorEntityMapper,
                jakarta.persistence.EntityManager entityManager) {
            return new JpaDebtorIntakeTransactionalWorker(
                    intakeRequestRepository,
                    debtorRepository,
                    debtorEntityMapper,
                    entityManager);
        }

        @Bean
        JpaDebtIntakeTransactionalWorker jpaDebtIntakeTransactionalWorker(
                SpringDataIntakeRequestRepository intakeRequestRepository,
                SpringDataDebtRepository debtRepository,
                SpringDataAccountingEntryRepository accountingEntryRepository,
                SpringDataDebtorRepository debtorRepository,
                DebtEntityMapper debtEntityMapper,
                AccountingEntryEntityMapper accountingEntryEntityMapper,
                jakarta.persistence.EntityManager entityManager) {
            return new JpaDebtIntakeTransactionalWorker(
                    intakeRequestRepository,
                    debtRepository,
                    accountingEntryRepository,
                    debtorRepository,
                    debtEntityMapper,
                    accountingEntryEntityMapper,
                    entityManager);
        }

        @Aspect
        class AccountingEntrySaveFailureAspect {

            @Around("execution(* com.pipelinepro.adapter.out.persistence.repository.SpringDataAccountingEntryRepository.saveAndFlush(..))")
            Object failAccountingWriteWhenEnabled(ProceedingJoinPoint joinPoint) throws Throwable {
                if (FAIL_ACCOUNTING_WRITE.get()) {
                    throw new DataIntegrityViolationException("accounting-write-failure");
                }
                return joinPoint.proceed();
            }
        }
    }
}
