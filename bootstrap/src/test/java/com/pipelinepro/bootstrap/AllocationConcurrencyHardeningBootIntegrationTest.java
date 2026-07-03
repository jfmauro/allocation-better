package com.pipelinepro.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.pipelinepro.PipelineProApplication;
import com.pipelinepro.adapter.out.persistence.entity.DebtEntity;
import com.pipelinepro.adapter.out.persistence.entity.DebtorEntity;
import com.pipelinepro.adapter.out.persistence.entity.PaymentEntity;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentAllocationRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.DebtorType;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.PaymentStatus;
import com.pipelinepro.domain.port.in.ExecuteAllocationUseCase;
import com.pipelinepro.domain.port.in.command.ExecuteAllocationCommand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = PipelineProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AllocationConcurrencyHardeningBootIntegrationTest {

    @Autowired
    private ExecuteAllocationUseCase executeAllocationUseCase;

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
    void should_keep_single_effective_allocation_when_idempotent_requests_are_submitted_concurrently() throws Exception {
        PersistedFixture fixture = persistFixture(new BigDecimal("100.00"));
        Instant now = Instant.now();

        ExecuteAllocationCommand command = new ExecuteAllocationCommand(
                fixture.paymentId,
                fixture.debtId,
                null,
                new BigDecimal("60.00"),
                "BOOT-IDEMP-1",
                "BOOT-CMD-1",
                "bootstrap-tester",
                now);

        List<PaymentAllocation> allocations = executeConcurrently(12, command);

        assertThat(Set.copyOf(allocations.stream().map(PaymentAllocation::id).toList())).hasSize(1);
        assertThat(springDataPaymentAllocationRepository.count()).isEqualTo(1L);

        PaymentEntity payment = springDataPaymentRepository.findById(fixture.paymentId).orElseThrow();
        DebtEntity debt = springDataDebtRepository.findById(fixture.debtId).orElseThrow();
        assertThat(payment.getRemainingAmount()).isEqualByComparingTo("40.00");
        assertThat(debt.getRemainingAmount()).isEqualByComparingTo("40.00");
        assertThat(payment.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(debt.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    void should_rollback_failed_attempts_when_concurrent_requests_exceed_remaining_capacity() throws Exception {
        PersistedFixture fixture = persistFixture(new BigDecimal("100.00"));
        Instant now = Instant.now();

        int threadCount = 8;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<PaymentAllocation>> futures = new ArrayList<>();

        for (int index = 0; index < threadCount; index++) {
            int sequence = index;
            ExecuteAllocationCommand command = new ExecuteAllocationCommand(
                    fixture.paymentId,
                    fixture.debtId,
                    null,
                    new BigDecimal("30.00"),
                    "BOOT-IDEMP-CONTENTION-" + sequence,
                    "BOOT-CMD-CONTENTION-" + sequence,
                    "bootstrap-tester",
                    now.plusSeconds(sequence));
            futures.add(executorService.submit(() -> {
                startLatch.await(10, TimeUnit.SECONDS);
                return executeAllocationUseCase.executeAllocation(command);
            }));
        }

        startLatch.countDown();

        int successCount = 0;
        int failureCount = 0;
        for (Future<PaymentAllocation> future : futures) {
            try {
                future.get(20, TimeUnit.SECONDS);
                successCount++;
            } catch (ExecutionException executionException) {
                failureCount++;
            }
        }

        executorService.shutdown();
        assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(successCount).isEqualTo(3);
        assertThat(failureCount).isEqualTo(5);
        assertThat(springDataPaymentAllocationRepository.count()).isEqualTo(3L);

        PaymentEntity payment = springDataPaymentRepository.findById(fixture.paymentId).orElseThrow();
        DebtEntity debt = springDataDebtRepository.findById(fixture.debtId).orElseThrow();
        assertThat(payment.getRemainingAmount()).isEqualByComparingTo("10.00");
        assertThat(debt.getRemainingAmount()).isEqualByComparingTo("10.00");
        assertThat(payment.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(debt.getRemainingAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);

        long allocationAuditEvents = springDataAuditEventRepository.findAll().stream()
                .filter(event -> "PAYMENT_ALLOCATED".equals(event.getEventType()))
                .count();
        assertThat(allocationAuditEvents).isEqualTo(3L);
    }

    private List<PaymentAllocation> executeConcurrently(int threads, ExecuteAllocationCommand command) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<PaymentAllocation>> futures = new ArrayList<>();
        for (int index = 0; index < threads; index++) {
            futures.add(executorService.submit(() -> {
                startLatch.await(10, TimeUnit.SECONDS);
                return executeAllocationUseCase.executeAllocation(command);
            }));
        }

        startLatch.countDown();
        List<PaymentAllocation> results = new ArrayList<>();
        for (Future<PaymentAllocation> future : futures) {
            results.add(future.get(20, TimeUnit.SECONDS));
        }
        executorService.shutdown();
        assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        return results;
    }

    private PersistedFixture persistFixture(BigDecimal amount) {
        Instant now = Instant.now();

        DebtorEntity debtor = new DebtorEntity();
        debtor.setType(DebtorType.NATURAL_PERSON);
        debtor.setDisplayName("Boot Debtor");
        debtor.setNationalNumber("85073003328");
        debtor.setActive(true);
        debtor.setCreatedAt(now);
        DebtorEntity savedDebtor = springDataDebtorRepository.saveAndFlush(debtor);

        PaymentEntity payment = new PaymentEntity();
        payment.setBankTransactionReference("TX-BOOT-" + UUID.randomUUID());
        payment.setAmount(amount);
        payment.setRemainingAmount(amount);
        payment.setCurrency("EUR");
        payment.setStatus(PaymentStatus.RECEIVED);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        PaymentEntity savedPayment = springDataPaymentRepository.saveAndFlush(payment);

        DebtEntity debt = new DebtEntity();
        debt.setDebtorId(savedDebtor.getId());
        debt.setReference("DEBT-BOOT-" + UUID.randomUUID());
        debt.setOriginalAmount(amount);
        debt.setRemainingAmount(amount);
        debt.setCurrency("EUR");
        debt.setStatus(DebtStatus.OPEN);
        debt.setCreatedAt(now);
        debt.setUpdatedAt(now);
        DebtEntity savedDebt = springDataDebtRepository.saveAndFlush(debt);

        return new PersistedFixture(savedPayment.getId(), savedDebt.getId());
    }

    private record PersistedFixture(UUID paymentId, UUID debtId) {
    }
}
