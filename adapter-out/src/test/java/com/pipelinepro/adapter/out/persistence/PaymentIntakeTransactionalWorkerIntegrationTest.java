package com.pipelinepro.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pipelinepro.adapter.out.persistence.impl.JpaAccountingEntryRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaAuditEventGateway;
import com.pipelinepro.adapter.out.persistence.impl.JpaPaymentIntakeTransactionalWorker;
import com.pipelinepro.adapter.out.persistence.impl.JpaPaymentRepository;
import com.pipelinepro.adapter.out.persistence.mapper.AccountingEntryEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.AuditEventEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.PaymentEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAccountingEntryRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.in.MatchPaymentUseCase;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import com.pipelinepro.domain.port.in.result.MatchPaymentResult;
import com.pipelinepro.domain.port.out.AuditEventGateway;
import com.pipelinepro.domain.port.out.AccountingEntryRepository;
import com.pipelinepro.domain.port.out.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest(classes = TestJpaConfiguration.class)
@Import(PaymentIntakeTransactionalWorkerIntegrationTest.PaymentIntakeWorkerTestConfiguration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PaymentIntakeTransactionalWorkerIntegrationTest {

    private static final AtomicBoolean FAIL_ACCOUNTING_WRITE = new AtomicBoolean(false);
    private static final AtomicBoolean FAIL_MATCHING = new AtomicBoolean(false);
    private static final AtomicInteger MATCH_PAYMENT_INVOCATIONS = new AtomicInteger(0);
    private static volatile CountDownLatch PAYMENT_SAVE_READY;
    private static volatile CountDownLatch PAYMENT_SAVE_BARRIER;

    @Autowired
    private JpaPaymentIntakeTransactionalWorker paymentIntakeWorker;

    @Autowired
    private SpringDataPaymentRepository springDataPaymentRepository;

    @Autowired
    private SpringDataAccountingEntryRepository springDataAccountingEntryRepository;

    @Autowired
    private SpringDataAuditEventRepository springDataAuditEventRepository;

    @AfterEach
    void cleanDatabase() {
        FAIL_ACCOUNTING_WRITE.set(false);
        FAIL_MATCHING.set(false);
        MATCH_PAYMENT_INVOCATIONS.set(0);
        PAYMENT_SAVE_READY = null;
        PAYMENT_SAVE_BARRIER = null;
        springDataAuditEventRepository.deleteAll();
        springDataAccountingEntryRepository.deleteAll();
        springDataPaymentRepository.deleteAll();
    }

    @Test
    void should_rollback_payment_accounting_and_audit_when_accounting_append_fails() {
        FAIL_ACCOUNTING_WRITE.set(true);
        Instant valueDate = Instant.parse("2026-03-02T10:00:00Z");
        Instant receivedAt = Instant.parse("2026-03-02T10:05:00Z");
        ReceivePaymentCommand command = new ReceivePaymentCommand(
                UUID.randomUUID(),
                "TX-ROLLBACK-ACCOUNTING-1",
                valueDate.minusSeconds(60),
                valueDate,
                new BigDecimal("75.00"),
                "EUR",
                "+++123/4567/89012+++",
                "invoice 2026/03",
                null,
                "Rollback Payer",
                "BE**4321",
                receivedAt);

        assertThatThrownBy(() -> paymentIntakeWorker.receivePayment(command))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("accounting-write-failure");

        assertThat(springDataPaymentRepository.count()).isZero();
        assertThat(springDataAccountingEntryRepository.count()).isZero();
        assertThat(springDataAuditEventRepository.count()).isZero();
        assertThat(MATCH_PAYMENT_INVOCATIONS.get()).isZero();
    }

    @Test
    void should_persist_one_payment_arrival_with_bank_value_date() {
        Instant valueDate = Instant.parse("2026-03-02T10:00:00Z");
        Instant receivedAt = Instant.parse("2026-03-02T10:05:00Z");
        ReceivePaymentCommand command = paymentCommand("TX-SUCCESS-1", valueDate, receivedAt);

        Payment persistedPayment = paymentIntakeWorker.receivePayment(command);

        assertThat(persistedPayment.id()).isNotNull();
        assertThat(springDataPaymentRepository.count()).isEqualTo(1);
        assertThat(springDataAccountingEntryRepository.findAll())
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getEventType()).isEqualTo(AccountingEventType.PAYMENT_ARRIVAL);
                    assertThat(entry.getSourceAggregateId()).isEqualTo(persistedPayment.id());
                    assertThat(entry.getOccurredAt()).isEqualTo(valueDate);
                });
        assertThat(MATCH_PAYMENT_INVOCATIONS.get()).isEqualTo(1);
    }

    @Test
    void should_keep_committed_payment_accounting_and_audit_when_post_commit_matching_fails() {
        FAIL_MATCHING.set(true);
        Instant valueDate = Instant.parse("2026-03-02T10:00:00Z");
        Instant receivedAt = Instant.parse("2026-03-02T10:05:00Z");
        ReceivePaymentCommand command = paymentCommand("TX-MATCHING-FAILURE-1", valueDate, receivedAt);

        Payment persistedPayment = paymentIntakeWorker.receivePayment(command);

        assertThat(persistedPayment.id()).isNotNull();
        assertThat(springDataPaymentRepository.count()).isEqualTo(1);
        assertThat(springDataAccountingEntryRepository.findAll())
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getEventType()).isEqualTo(AccountingEventType.PAYMENT_ARRIVAL);
                    assertThat(entry.getSourceAggregateId()).isEqualTo(persistedPayment.id());
                    assertThat(entry.getOccurredAt()).isEqualTo(valueDate);
                });
        assertThat(springDataAuditEventRepository.findAll())
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getAggregateType()).isEqualTo("PAYMENT");
                    assertThat(event.getAggregateId()).isEqualTo(persistedPayment.id());
                    assertThat(event.getEventType()).isEqualTo("PAYMENT_RECEIVED");
                });
        assertThat(MATCH_PAYMENT_INVOCATIONS.get()).isEqualTo(1);
    }

    @Test
    void should_reject_duplicate_payment_without_creating_an_accounting_entry() {
        Instant receivedAt = Instant.parse("2026-03-02T10:05:00Z");
        ReceivePaymentCommand firstCommand = paymentCommand(
                "TX-DUPLICATE-1",
                receivedAt.minusSeconds(300),
                receivedAt);
        ReceivePaymentCommand duplicateCommand = paymentCommand(
                firstCommand.bankTransactionReference(),
                receivedAt.minusSeconds(120),
                receivedAt);

        paymentIntakeWorker.receivePayment(firstCommand);

        assertThatThrownBy(() -> paymentIntakeWorker.receivePayment(duplicateCommand))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Payment already exists for bankTransactionReference");

        assertThat(springDataPaymentRepository.count()).isEqualTo(1);
        assertThat(springDataAccountingEntryRepository.count()).isEqualTo(1);
    }

    @Test
    void should_map_concurrent_duplicate_payment_reference_to_one_stable_conflict() throws Exception {
        Instant receivedAt = Instant.parse("2026-03-02T10:05:00Z");
        String bankTransactionReference = "TX-CONCURRENT-DUPLICATE-1";
        ReceivePaymentCommand firstCommand = paymentCommand(
                bankTransactionReference,
                receivedAt.minusSeconds(300),
                receivedAt);
        ReceivePaymentCommand secondCommand = paymentCommand(
                bankTransactionReference,
                receivedAt.minusSeconds(240),
                receivedAt);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        PAYMENT_SAVE_READY = new CountDownLatch(2);
        PAYMENT_SAVE_BARRIER = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = List.of(
                    submitPayment(executor, startLatch, firstCommand),
                    submitPayment(executor, startLatch, secondCommand));
            startLatch.countDown();
            assertThat(PAYMENT_SAVE_READY.await(10, TimeUnit.SECONDS)).isTrue();
            PAYMENT_SAVE_BARRIER.countDown();

            List<Object> outcomes = new ArrayList<>();
            for (Future<Object> future : futures) {
                outcomes.add(future.get(20, TimeUnit.SECONDS));
            }

            assertThat(outcomes).filteredOn(Payment.class::isInstance).hasSize(1);
            assertThat(outcomes).filteredOn(IllegalStateException.class::isInstance)
                    .singleElement()
                    .satisfies(outcome -> assertThat((IllegalStateException) outcome)
                            .hasMessageContaining("Payment already exists for bankTransactionReference")
                            .hasCauseInstanceOf(DataIntegrityViolationException.class));
            assertThat(springDataPaymentRepository.count()).isEqualTo(1);
            assertThat(springDataAccountingEntryRepository.count()).isEqualTo(1);
        } finally {
            PAYMENT_SAVE_BARRIER.countDown();
            PAYMENT_SAVE_READY = null;
            PAYMENT_SAVE_BARRIER = null;
            executor.shutdownNow();
        }
    }

    private Future<Object> submitPayment(
            ExecutorService executor,
            CountDownLatch startLatch,
            ReceivePaymentCommand command) {
        Callable<Object> task = () -> {
            if (!startLatch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Payment intake concurrency test did not start");
            }
            try {
                return paymentIntakeWorker.receivePayment(command);
            } catch (RuntimeException exception) {
                return exception;
            }
        };
        return executor.submit(task);
    }

    private ReceivePaymentCommand paymentCommand(
            String bankTransactionReference,
            Instant valueDate,
            Instant receivedAt) {
        return new ReceivePaymentCommand(
                UUID.randomUUID(),
                bankTransactionReference,
                valueDate.minusSeconds(60),
                valueDate,
                new BigDecimal("75.00"),
                "EUR",
                "+++123/4567/89012+++",
                "payment test",
                null,
                "Payment Payer",
                "BE**4321",
                receivedAt);
    }

    @TestConfiguration
    static class PaymentIntakeWorkerTestConfiguration {

        @Bean
        PaymentEntityMapper paymentEntityMapper() {
            return Mappers.getMapper(PaymentEntityMapper.class);
        }

        @Bean
        AccountingEntryEntityMapper accountingEntryEntityMapper() {
            return Mappers.getMapper(AccountingEntryEntityMapper.class);
        }

        @Bean
        AuditEventEntityMapper auditEventEntityMapper() {
            return Mappers.getMapper(AuditEventEntityMapper.class);
        }

        @Bean
        PaymentRepository paymentRepository(
                SpringDataPaymentRepository springDataPaymentRepository,
                PaymentEntityMapper paymentEntityMapper) {
            return new JpaPaymentRepository(springDataPaymentRepository, paymentEntityMapper);
        }

        @Bean
        AccountingEntryRepository accountingEntryRepository(
                SpringDataAccountingEntryRepository springDataAccountingEntryRepository,
                AccountingEntryEntityMapper accountingEntryEntityMapper) {
            return new JpaAccountingEntryRepository(springDataAccountingEntryRepository, accountingEntryEntityMapper);
        }

        @Bean
        AuditEventGateway auditEventGateway(
                SpringDataAuditEventRepository springDataAuditEventRepository,
                AuditEventEntityMapper auditEventEntityMapper) {
            return new JpaAuditEventGateway(springDataAuditEventRepository, auditEventEntityMapper);
        }

        @Bean
        MatchPaymentUseCase matchPaymentUseCase() {
            return command -> {
                MATCH_PAYMENT_INVOCATIONS.incrementAndGet();
                if (FAIL_MATCHING.get()) {
                    throw new IllegalStateException("matching-failure");
                }
                return MatchPaymentResult.autoAllocated(MatchingMethod.NAME, "stub-match");
            };
        }

        @Bean
        JpaPaymentIntakeTransactionalWorker jpaPaymentIntakeTransactionalWorker(
                PaymentRepository paymentRepository,
                AuditEventGateway auditEventGateway,
                MatchPaymentUseCase matchPaymentUseCase,
                AccountingEntryRepository accountingEntryRepository,
                PlatformTransactionManager transactionManager) {
            return new JpaPaymentIntakeTransactionalWorker(
                    paymentRepository,
                    auditEventGateway,
                    matchPaymentUseCase,
                    accountingEntryRepository,
                    transactionManager);
        }

        @Bean
        AccountingEntrySaveFailureAspect accountingEntrySaveFailureAspect() {
            return new AccountingEntrySaveFailureAspect();
        }

        @Bean
        PaymentSaveRaceAspect paymentSaveRaceAspect() {
            return new PaymentSaveRaceAspect();
        }
    }

    @Aspect
    static class AccountingEntrySaveFailureAspect {

        @Around("execution(* com.pipelinepro.adapter.out.persistence.repository.SpringDataAccountingEntryRepository.saveAndFlush(..))")
        Object failAccountingWriteWhenEnabled(ProceedingJoinPoint joinPoint) throws Throwable {
            if (FAIL_ACCOUNTING_WRITE.get()) {
                throw new DataIntegrityViolationException("accounting-write-failure");
            }
            return joinPoint.proceed();
        }
    }

    @Aspect
    static class PaymentSaveRaceAspect {

        @Around("execution(* com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository.saveAndFlush(..))")
        Object holdConcurrentPaymentSaves(ProceedingJoinPoint joinPoint) throws Throwable {
            CountDownLatch ready = PAYMENT_SAVE_READY;
            CountDownLatch barrier = PAYMENT_SAVE_BARRIER;
            if (ready != null && barrier != null) {
                ready.countDown();
                if (!barrier.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Payment save concurrency test barrier timed out");
                }
            }
            return joinPoint.proceed();
        }
    }
}
