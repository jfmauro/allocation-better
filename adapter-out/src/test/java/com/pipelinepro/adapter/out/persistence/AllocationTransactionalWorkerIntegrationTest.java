package com.pipelinepro.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pipelinepro.adapter.out.persistence.entity.AllocationProposalEntity;
import com.pipelinepro.adapter.out.persistence.impl.JpaAllocationTransactionalWorker;
import com.pipelinepro.adapter.out.persistence.mapper.AllocationProposalEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.AuditEventEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtorEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.PaymentAllocationEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.PaymentEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentAllocationRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.ProposalStatus;
import com.pipelinepro.domain.port.out.AllocationTransactionalWorker;
import com.pipelinepro.domain.port.out.command.AllocationExecutionRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
@Import(AllocationTransactionalWorkerIntegrationTest.TransactionalWorkerTestConfiguration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AllocationTransactionalWorkerIntegrationTest {

    private static final AtomicBoolean FAIL_AUDIT_WRITE = new AtomicBoolean(false);

    private final PaymentEntityMapper paymentEntityMapper = Mappers.getMapper(PaymentEntityMapper.class);
    private final DebtorEntityMapper debtorEntityMapper = Mappers.getMapper(DebtorEntityMapper.class);
    private final DebtEntityMapper debtEntityMapper = Mappers.getMapper(DebtEntityMapper.class);

    @Autowired
    private AllocationTransactionalWorker allocationTransactionalWorker;

    @Autowired
    private SpringDataPaymentRepository springDataPaymentRepository;

    @Autowired
    private SpringDataDebtorRepository springDataDebtorRepository;

    @Autowired
    private SpringDataDebtRepository springDataDebtRepository;

    @Autowired
    private SpringDataPaymentAllocationRepository springDataPaymentAllocationRepository;

    @Autowired
    private SpringDataAuditEventRepository springDataAuditEventRepository;

    @Autowired
    private SpringDataAllocationProposalRepository springDataAllocationProposalRepository;

    @AfterEach
    void cleanDatabase() {
        springDataAuditEventRepository.deleteAll();
        springDataPaymentAllocationRepository.deleteAll();
        springDataAllocationProposalRepository.deleteAll();
        springDataDebtRepository.deleteAll();
        springDataDebtorRepository.deleteAll();
        springDataPaymentRepository.deleteAll();
    }

    @Test
    void should_keep_single_effective_allocation_for_concurrent_idempotent_requests() throws Exception {
        var fixture = persistPaymentAndDebt(new BigDecimal("100.00"));
        Instant now = Instant.now();

        AllocationExecutionRequest request = new AllocationExecutionRequest(
                fixture.paymentId,
                fixture.debtId,
                null,
                new BigDecimal("60.00"),
                "IDEMP-CONCURRENT-1",
                "CMD-CONCURRENT-1",
                "worker-user",
                now);

        List<UUID> allocationIds = executeConcurrent(8, () -> allocationTransactionalWorker.executeAllocation(request).id());

        assertThat(Set.copyOf(allocationIds)).hasSize(1);
        assertThat(springDataPaymentAllocationRepository.count()).isEqualTo(1L);

        var savedPayment = springDataPaymentRepository.findById(fixture.paymentId).orElseThrow();
        var savedDebt = springDataDebtRepository.findById(fixture.debtId).orElseThrow();
        assertThat(savedPayment.getRemainingAmount()).isEqualByComparingTo("40.00");
        assertThat(savedDebt.getRemainingAmount()).isEqualByComparingTo("40.00");
        assertThat(savedPayment.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(savedDebt.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);

        var allocationId = springDataPaymentAllocationRepository
                .findByIdempotencyKey("IDEMP-CONCURRENT-1")
                .orElseThrow()
                .getId();
        List<String> eventTypes = springDataAuditEventRepository.findAll().stream()
                .map(event -> event.getEventType())
                .toList();
        assertThat(eventTypes)
                .hasSize(1)
                .containsExactly("PAYMENT_ALLOCATED");

        assertThat(springDataAuditEventRepository.findAll())
                .anySatisfy(event -> {
                    assertThat(event.getAggregateType()).isEqualTo("PAYMENT_ALLOCATION");
                    assertThat(event.getAggregateId()).isEqualTo(allocationId);
                    assertThat(event.getEventType()).isEqualTo("PAYMENT_ALLOCATED");
                });
    }

    @Test
    void should_write_manual_validation_audit_event_type_when_proposal_is_present() {
        var fixture = persistPaymentAndDebt(new BigDecimal("100.00"));
        Instant now = Instant.now();

        AllocationProposalEntity proposalEntity = new AllocationProposalEntity();
        proposalEntity.setId(null);
        proposalEntity.setPaymentId(fixture.paymentId);
        proposalEntity.setStatus(ProposalStatus.PROPOSED);
        proposalEntity.setMatchingMethod(MatchingMethod.NAME);
        proposalEntity.setReason("manual review required");
        proposalEntity.setVersion(0L);
        proposalEntity.setCreatedAt(now);
        proposalEntity.setUpdatedAt(now);
        AllocationProposalEntity savedProposal = springDataAllocationProposalRepository.saveAndFlush(proposalEntity);

        AllocationExecutionRequest request = new AllocationExecutionRequest(
                fixture.paymentId,
                fixture.debtId,
                savedProposal.getId(),
                new BigDecimal("25.00"),
                "IDEMP-MANUAL-1",
                "CMD-MANUAL-1",
                "reviewer-user",
                now.plusSeconds(1));

        var allocation = allocationTransactionalWorker.executeAllocation(request);

        var auditEvents = springDataAuditEventRepository.findAll();

        assertThat(auditEvents)
                .hasSize(2)
                .extracting(event -> event.getEventType())
                .containsExactlyInAnyOrder("USER_VALIDATED_ALLOCATION", "PAYMENT_ALLOCATED");

        assertThat(auditEvents)
                .anySatisfy(event -> {
                    assertThat(event.getAggregateType()).isEqualTo("ALLOCATION_PROPOSAL");
                    assertThat(event.getAggregateId()).isEqualTo(savedProposal.getId());
                    assertThat(event.getEventType()).isEqualTo("USER_VALIDATED_ALLOCATION");
                    assertThat(event.getPayloadJson()).contains("\"validationReason\":\"manual review required\"");
                })
                .anySatisfy(event -> {
                    assertThat(event.getAggregateType()).isEqualTo("PAYMENT_ALLOCATION");
                    assertThat(event.getAggregateId()).isEqualTo(allocation.id());
                    assertThat(event.getEventType()).isEqualTo("PAYMENT_ALLOCATED");
                });
    }

    @Test
    void should_prevent_negative_remaining_amount_under_contention() throws Exception {
        var fixture = persistPaymentAndDebt(new BigDecimal("100.00"));
        Instant now = Instant.now();

        AllocationExecutionRequest first = new AllocationExecutionRequest(
                fixture.paymentId,
                fixture.debtId,
                null,
                new BigDecimal("70.00"),
                "IDEMP-CONTENTION-1",
                "CMD-CONTENTION-1",
                "worker-user",
                now);

        AllocationExecutionRequest second = new AllocationExecutionRequest(
                fixture.paymentId,
                fixture.debtId,
                null,
                new BigDecimal("70.00"),
                "IDEMP-CONTENTION-2",
                "CMD-CONTENTION-2",
                "worker-user",
                now.plusSeconds(1));

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        Future<?> firstFuture = executorService.submit(() -> executeWithLatch(startLatch, first));
        Future<?> secondFuture = executorService.submit(() -> executeWithLatch(startLatch, second));
        startLatch.countDown();

        int successCount = 0;
        int failureCount = 0;
        for (Future<?> future : List.of(firstFuture, secondFuture)) {
            try {
                future.get(15, TimeUnit.SECONDS);
                successCount++;
            } catch (ExecutionException exception) {
                failureCount++;
            }
        }
        executorService.shutdownNow();

        assertThat(successCount).isEqualTo(1);
        assertThat(failureCount).isEqualTo(1);
        assertThat(springDataPaymentAllocationRepository.count()).isEqualTo(1L);

        var savedPayment = springDataPaymentRepository.findById(fixture.paymentId).orElseThrow();
        var savedDebt = springDataDebtRepository.findById(fixture.debtId).orElseThrow();
        assertThat(savedPayment.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(savedDebt.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(savedPayment.getRemainingAmount()).isEqualByComparingTo("30.00");
        assertThat(savedDebt.getRemainingAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    void should_keep_single_effective_allocation_when_same_command_is_submitted_with_different_idempotency_keys() throws Exception {
        var fixture = persistPaymentAndDebt(new BigDecimal("100.00"));
        Instant now = Instant.now();

        AllocationExecutionRequest first = new AllocationExecutionRequest(
                fixture.paymentId,
                fixture.debtId,
                null,
                new BigDecimal("60.00"),
                "IDEMP-CMD-COLLISION-1",
                "CMD-COLLISION-1",
                "worker-user",
                now);

        AllocationExecutionRequest second = new AllocationExecutionRequest(
                fixture.paymentId,
                fixture.debtId,
                null,
                new BigDecimal("60.00"),
                "IDEMP-CMD-COLLISION-2",
                "CMD-COLLISION-1",
                "worker-user",
                now.plusSeconds(1));

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        Future<UUID> firstFuture = executorService.submit(() -> executeWithLatchAndReturnId(startLatch, first));
        Future<UUID> secondFuture = executorService.submit(() -> executeWithLatchAndReturnId(startLatch, second));
        startLatch.countDown();

        int successCount = 0;
        int failureCount = 0;
        for (Future<UUID> future : List.of(firstFuture, secondFuture)) {
            try {
                future.get(15, TimeUnit.SECONDS);
                successCount++;
            } catch (ExecutionException executionException) {
                failureCount++;
            }
        }
        executorService.shutdown();
        assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(successCount).isEqualTo(1);
        assertThat(failureCount).isEqualTo(1);
        assertThat(springDataPaymentAllocationRepository.count()).isEqualTo(1L);

        var persistedAllocation = springDataPaymentAllocationRepository.findAll().getFirst();
        assertThat(persistedAllocation.getCommandId()).isEqualTo("CMD-COLLISION-1");
        assertThat(persistedAllocation.getIdempotencyKey())
                .isIn("IDEMP-CMD-COLLISION-1", "IDEMP-CMD-COLLISION-2");

        var savedPayment = springDataPaymentRepository.findById(fixture.paymentId).orElseThrow();
        var savedDebt = springDataDebtRepository.findById(fixture.debtId).orElseThrow();
        assertThat(savedPayment.getRemainingAmount()).isEqualByComparingTo("40.00");
        assertThat(savedDebt.getRemainingAmount()).isEqualByComparingTo("40.00");
        assertThat(savedPayment.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(savedDebt.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    void should_keep_non_negative_balances_when_concurrent_requests_exceed_available_capacity() throws Exception {
        var fixture = persistPaymentAndDebt(new BigDecimal("100.00"));
        Instant now = Instant.now();

        int threadCount = 10;
        BigDecimal requestedAmount = new BigDecimal("15.00");
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<UUID>> futures = new ArrayList<>();
        for (int index = 0; index < threadCount; index++) {
            int sequence = index;
            AllocationExecutionRequest request = new AllocationExecutionRequest(
                    fixture.paymentId,
                    fixture.debtId,
                    null,
                    requestedAmount,
                    "IDEMP-STRESS-" + sequence,
                    "CMD-STRESS-" + sequence,
                    "worker-user",
                    now.plusSeconds(sequence));
            futures.add(executorService.submit(() -> executeWithLatchAndReturnId(startLatch, request)));
        }
        startLatch.countDown();

        int successCount = 0;
        int failureCount = 0;
        for (Future<UUID> future : futures) {
            try {
                future.get(20, TimeUnit.SECONDS);
                successCount++;
            } catch (ExecutionException executionException) {
                failureCount++;
            }
        }
        executorService.shutdown();
        assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(successCount).isEqualTo(6);
        assertThat(failureCount).isEqualTo(4);
        assertThat(springDataPaymentAllocationRepository.count()).isEqualTo(6L);

        var savedPayment = springDataPaymentRepository.findById(fixture.paymentId).orElseThrow();
        var savedDebt = springDataDebtRepository.findById(fixture.debtId).orElseThrow();
        assertThat(savedPayment.getRemainingAmount()).isEqualByComparingTo("10.00");
        assertThat(savedDebt.getRemainingAmount()).isEqualByComparingTo("10.00");
        assertThat(savedPayment.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(savedDebt.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);

        long allocationAuditEvents = springDataAuditEventRepository.findAll().stream()
                .filter(event -> "PAYMENT_ALLOCATED".equals(event.getEventType()))
                .count();
        assertThat(allocationAuditEvents).isEqualTo(6L);
    }

    @Test
    void should_rollback_allocation_when_audit_write_fails() {
        var fixture = persistPaymentAndDebt(new BigDecimal("100.00"));
        FAIL_AUDIT_WRITE.set(true);

        try {
            AllocationExecutionRequest request = new AllocationExecutionRequest(
                    fixture.paymentId,
                    fixture.debtId,
                    null,
                    new BigDecimal("40.00"),
                    "IDEMP-ROLLBACK-1",
                    "CMD-ROLLBACK-1",
                    "worker-user",
                    Instant.now());

            assertThatThrownBy(() -> allocationTransactionalWorker.executeAllocation(request))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("audit-write-failure");

            assertThat(springDataPaymentAllocationRepository.findByIdempotencyKey("IDEMP-ROLLBACK-1")).isEmpty();
            assertThat(springDataAuditEventRepository.count()).isZero();
            var payment = springDataPaymentRepository.findById(fixture.paymentId).orElseThrow();
            var debt = springDataDebtRepository.findById(fixture.debtId).orElseThrow();
            assertThat(payment.getRemainingAmount()).isEqualByComparingTo("100.00");
            assertThat(debt.getRemainingAmount()).isEqualByComparingTo("100.00");
        } finally {
            FAIL_AUDIT_WRITE.set(false);
        }
    }

    private PersistedFixture persistPaymentAndDebt(BigDecimal amount) {
        Instant now = Instant.now();
        Debtor debtor = Debtor.activeNaturalPerson(
                UUID.randomUUID(),
                "Worker Debtor",
                "85073003328",
                now);
        var debtorEntity = debtorEntityMapper.toEntity(debtor);
        debtorEntity.setId(null);
        var savedDebtor = springDataDebtorRepository.saveAndFlush(debtorEntity);

        Payment payment = Payment.received(
                UUID.randomUUID(),
                "TX-WORKER-" + UUID.randomUUID(),
                amount,
                "EUR",
                null,
                "worker test",
                "Payer",
                "BE**0000",
                now);
        var paymentEntity = paymentEntityMapper.toEntity(payment);
        paymentEntity.setId(null);
        var savedPayment = springDataPaymentRepository.saveAndFlush(paymentEntity);

        Debt debt = Debt.open(
                UUID.randomUUID(),
                savedDebtor.getId(),
                "DEBT-WORKER-" + UUID.randomUUID(),
                amount,
                "EUR",
                null,
                now);
        var debtEntity = debtEntityMapper.toEntity(debt);
        debtEntity.setId(null);
        var savedDebt = springDataDebtRepository.saveAndFlush(debtEntity);

        return new PersistedFixture(savedPayment.getId(), savedDebt.getId());
    }

    private <T> List<T> executeConcurrent(int threads, Callable<T> callable) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<T>> futures = new ArrayList<>();
        for (int index = 0; index < threads; index++) {
            futures.add(executorService.submit(() -> {
                startLatch.await(10, TimeUnit.SECONDS);
                return callable.call();
            }));
        }

        startLatch.countDown();
        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) {
            results.add(future.get(20, TimeUnit.SECONDS));
        }
        executorService.shutdownNow();
        return results;
    }

    private void executeWithLatch(CountDownLatch startLatch, AllocationExecutionRequest request) {
        try {
            startLatch.await(10, TimeUnit.SECONDS);
            allocationTransactionalWorker.executeAllocation(request);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interruptedException);
        }
    }

    private UUID executeWithLatchAndReturnId(CountDownLatch startLatch, AllocationExecutionRequest request) {
        try {
            startLatch.await(10, TimeUnit.SECONDS);
            return allocationTransactionalWorker.executeAllocation(request).id();
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interruptedException);
        }
    }

    private record PersistedFixture(UUID paymentId, UUID debtId) {
    }

    @TestConfiguration
    static class TransactionalWorkerTestConfiguration {

        @Bean
        AuditEventSaveFailureAspect auditEventSaveFailureAspect() {
            return new AuditEventSaveFailureAspect();
        }

        @Bean
        PaymentEntityMapper paymentEntityMapper() {
            return Mappers.getMapper(PaymentEntityMapper.class);
        }

        @Bean
        DebtEntityMapper debtEntityMapper() {
            return Mappers.getMapper(DebtEntityMapper.class);
        }

        @Bean
        PaymentAllocationEntityMapper paymentAllocationEntityMapper() {
            return Mappers.getMapper(PaymentAllocationEntityMapper.class);
        }

        @Bean
        AuditEventEntityMapper auditEventEntityMapper() {
            return Mappers.getMapper(AuditEventEntityMapper.class);
        }

        @Bean
        AllocationProposalEntityMapper allocationProposalEntityMapper() {
            return Mappers.getMapper(AllocationProposalEntityMapper.class);
        }

        @Bean
        AllocationTransactionalWorker allocationTransactionalWorker(
                SpringDataPaymentRepository springDataPaymentRepository,
                SpringDataDebtRepository springDataDebtRepository,
                SpringDataAllocationProposalRepository springDataAllocationProposalRepository,
                SpringDataPaymentAllocationRepository springDataPaymentAllocationRepository,
                SpringDataAuditEventRepository springDataAuditEventRepository,
                PaymentEntityMapper paymentEntityMapper,
                DebtEntityMapper debtEntityMapper,
                PaymentAllocationEntityMapper paymentAllocationEntityMapper,
                AuditEventEntityMapper auditEventEntityMapper,
                AllocationProposalEntityMapper allocationProposalEntityMapper) {
            return new JpaAllocationTransactionalWorker(
                    springDataPaymentRepository,
                    springDataDebtRepository,
                    springDataAllocationProposalRepository,
                    springDataPaymentAllocationRepository,
                    springDataAuditEventRepository,
                    paymentEntityMapper,
                    debtEntityMapper,
                    paymentAllocationEntityMapper,
                    auditEventEntityMapper,
                    allocationProposalEntityMapper);
        }
    }

    @Aspect
    static class AuditEventSaveFailureAspect {

        @Around("execution(* com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository.save(..))")
        Object failAuditWriteWhenEnabled(ProceedingJoinPoint joinPoint) throws Throwable {
            if (FAIL_AUDIT_WRITE.get()) {
                throw new DataIntegrityViolationException("audit-write-failure");
            }
            return joinPoint.proceed();
        }
    }
}
