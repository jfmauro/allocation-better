package com.pipelinepro.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pipelinepro.adapter.out.persistence.entity.AllocationProposalEntity;
import com.pipelinepro.adapter.out.persistence.entity.DebtEntity;
import com.pipelinepro.adapter.out.persistence.entity.DebtorEntity;
import com.pipelinepro.adapter.out.persistence.entity.PaymentEntity;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.DebtorType;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.PaymentStatus;
import com.pipelinepro.domain.ProposalStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@DataJpaTest
class OptimisticLockingDataJpaTest {

    @Autowired
    private SpringDataPaymentRepository paymentRepository;

    @Autowired
    private SpringDataDebtorRepository debtorRepository;

    @Autowired
    private SpringDataDebtRepository debtRepository;

    @Autowired
    private SpringDataAllocationProposalRepository allocationProposalRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void should_fail_on_stale_payment_update() {
        PaymentEntity payment = paymentRepository.saveAndFlush(validPaymentEntity("TX-OPT-1"));
        entityManager.clear();

        PaymentEntity stale = paymentRepository.findById(payment.getId()).orElseThrow();
        entityManager.detach(stale);

        PaymentEntity fresh = paymentRepository.findById(payment.getId()).orElseThrow();
        fresh.setStatus(PaymentStatus.MATCH_PROPOSED);
        paymentRepository.saveAndFlush(fresh);

        stale.setStatus(PaymentStatus.ALLOCATED);

        assertThatThrownBy(() -> paymentRepository.saveAndFlush(stale))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void should_fail_on_stale_debt_update() {
        DebtorEntity debtor = debtorRepository.saveAndFlush(validDebtorEntity());
        DebtEntity debt = debtRepository.saveAndFlush(validDebtEntity(debtor.getId()));
        entityManager.clear();

        DebtEntity stale = debtRepository.findById(debt.getId()).orElseThrow();
        entityManager.detach(stale);

        DebtEntity fresh = debtRepository.findById(debt.getId()).orElseThrow();
        fresh.setStatus(DebtStatus.PARTIALLY_PAID);
        debtRepository.saveAndFlush(fresh);

        stale.setStatus(DebtStatus.PAID);

        assertThatThrownBy(() -> debtRepository.saveAndFlush(stale))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void should_fail_on_stale_allocation_proposal_update() {
        PaymentEntity payment = paymentRepository.saveAndFlush(validPaymentEntity("TX-OPT-2"));
        AllocationProposalEntity proposal = allocationProposalRepository.saveAndFlush(validProposalEntity(payment.getId()));
        entityManager.clear();

        AllocationProposalEntity stale = allocationProposalRepository.findById(proposal.getId()).orElseThrow();
        entityManager.detach(stale);

        AllocationProposalEntity fresh = allocationProposalRepository.findById(proposal.getId()).orElseThrow();
        fresh.setStatus(ProposalStatus.VALIDATED);
        allocationProposalRepository.saveAndFlush(fresh);

        stale.setStatus(ProposalStatus.REJECTED);

        assertThatThrownBy(() -> allocationProposalRepository.saveAndFlush(stale))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    private PaymentEntity validPaymentEntity(String reference) {
        PaymentEntity entity = new PaymentEntity();
        entity.setBankTransactionReference(reference);
        entity.setAmount(new BigDecimal("200.00"));
        entity.setRemainingAmount(new BigDecimal("200.00"));
        entity.setCurrency("EUR");
        entity.setStatus(PaymentStatus.RECEIVED);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private DebtorEntity validDebtorEntity() {
        DebtorEntity entity = new DebtorEntity();
        entity.setType(DebtorType.NATURAL_PERSON);
        entity.setDisplayName("Optimistic Debtor");
        entity.setNationalNumber("85073003328");
        entity.setActive(true);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private DebtEntity validDebtEntity(UUID debtorId) {
        DebtEntity entity = new DebtEntity();
        entity.setDebtorId(debtorId);
        entity.setReference("DEBT-OPT-1");
        entity.setOriginalAmount(new BigDecimal("200.00"));
        entity.setRemainingAmount(new BigDecimal("200.00"));
        entity.setCurrency("EUR");
        entity.setStatus(DebtStatus.OPEN);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private AllocationProposalEntity validProposalEntity(UUID paymentId) {
        AllocationProposalEntity entity = new AllocationProposalEntity();
        entity.setPaymentId(paymentId);
        entity.setStatus(ProposalStatus.PROPOSED);
        entity.setMatchingMethod(MatchingMethod.NAME);
        entity.setReason("created for optimistic lock test");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
