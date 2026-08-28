package com.pipelinepro.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.pipelinepro.adapter.out.persistence.entity.DebtorEntity;
import com.pipelinepro.adapter.out.persistence.impl.JpaAllocationProposalCandidateRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaAllocationProposalRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaDebtRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaPaymentRepository;
import com.pipelinepro.adapter.out.persistence.mapper.AllocationProposalCandidateEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.AllocationProposalEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtorEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.PaymentEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalCandidateRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.MatchConfidence;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentStatus;
import com.pipelinepro.domain.ProposalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class AdapterRepositorySaveUpdateDataJpaTest {

    private final PaymentEntityMapper paymentEntityMapper = Mappers.getMapper(PaymentEntityMapper.class);
    private final DebtEntityMapper debtEntityMapper = Mappers.getMapper(DebtEntityMapper.class);
    private final DebtorEntityMapper debtorEntityMapper = Mappers.getMapper(DebtorEntityMapper.class);
    private final AllocationProposalEntityMapper allocationProposalEntityMapper =
            Mappers.getMapper(AllocationProposalEntityMapper.class);
    private final AllocationProposalCandidateEntityMapper allocationProposalCandidateEntityMapper =
            Mappers.getMapper(AllocationProposalCandidateEntityMapper.class);

    @Autowired
    private SpringDataPaymentRepository springDataPaymentRepository;

    @Autowired
    private SpringDataDebtorRepository springDataDebtorRepository;

    @Autowired
    private SpringDataDebtRepository springDataDebtRepository;

    @Autowired
    private SpringDataAllocationProposalRepository springDataAllocationProposalRepository;

    @Autowired
    private SpringDataAllocationProposalCandidateRepository springDataAllocationProposalCandidateRepository;

    @Test
    void should_save_and_update_payment_via_adapter_repository() {
        JpaPaymentRepository adapterRepository = new JpaPaymentRepository(springDataPaymentRepository, paymentEntityMapper);
        Instant now = Instant.now();

        Payment payment = Payment.received(
                UUID.randomUUID(),
                "TX-ADAPTER-SAVE-1",
                new BigDecimal("200.00"),
                "EUR",
                null,
                "adapter save test",
                "Payer",
                "BE**1234",
                now);

        Payment persisted = adapterRepository.save(payment);
        assertThat(persisted.id()).isEqualTo(payment.id());
        assertThat(persisted.version()).isZero();

        persisted.allocate(new BigDecimal("50.00"), now.plusSeconds(1));
        Payment updated = adapterRepository.save(persisted);

        assertThat(updated.remainingAmount()).isEqualByComparingTo("150.00");
        assertThat(updated.status()).isEqualTo(PaymentStatus.PARTIALLY_ALLOCATED);
        assertThat(updated.version()).isGreaterThanOrEqualTo(1L);
        assertThat(adapterRepository.findById(updated.id())).isPresent();
    }

    @Test
    void should_save_and_update_debt_and_proposal_via_adapter_repositories() {
        JpaDebtRepository debtAdapterRepository = new JpaDebtRepository(springDataDebtRepository, debtEntityMapper);
        JpaAllocationProposalRepository proposalAdapterRepository =
                new JpaAllocationProposalRepository(springDataAllocationProposalRepository, allocationProposalEntityMapper);

        Instant now = Instant.now();
        Debtor debtor = Debtor.activeNaturalPerson(
                UUID.randomUUID(),
                "Adapter Debtor",
                "85073003328",
                now);
        DebtorEntity debtorEntity = debtorEntityMapper.toEntity(debtor);
        debtorEntity.setId(null);
        DebtorEntity savedDebtor = springDataDebtorRepository.saveAndFlush(debtorEntity);

        Debt debt = Debt.open(
                UUID.randomUUID(),
                savedDebtor.getId(),
                "DEBT-ADAPTER-SAVE-1",
                new BigDecimal("300.00"),
                "EUR",
                null,
                now);
        Debt persistedDebt = debtAdapterRepository.save(debt);
        persistedDebt.pay(new BigDecimal("75.00"), now.plusSeconds(1));
        Debt updatedDebt = debtAdapterRepository.save(persistedDebt);

        assertThat(updatedDebt.remainingAmount()).isEqualByComparingTo("225.00");
        assertThat(updatedDebt.version()).isGreaterThanOrEqualTo(1L);
        assertThat(debtAdapterRepository.findById(updatedDebt.id())).isPresent();

        Payment payment = Payment.received(
                UUID.randomUUID(),
                "TX-ADAPTER-SAVE-2",
                new BigDecimal("300.00"),
                "EUR",
                null,
                null,
                null,
                null,
                now);
        var paymentEntity = paymentEntityMapper.toEntity(payment);
        paymentEntity.setId(null);
        var savedPaymentEntity = springDataPaymentRepository.saveAndFlush(paymentEntity);
        Payment persistedPayment = paymentEntityMapper.toDomain(savedPaymentEntity);

        AllocationProposal proposal = AllocationProposal.proposed(
                UUID.randomUUID(),
                persistedPayment.id(),
                MatchingMethod.NAME,
                "adapter proposal",
                now);
        AllocationProposal persistedProposal = proposalAdapterRepository.save(proposal);
        persistedProposal.validate("reviewer", now.plusSeconds(2));
        AllocationProposal updatedProposal = proposalAdapterRepository.save(persistedProposal);

        assertThat(updatedProposal.status()).isEqualTo(ProposalStatus.VALIDATED);
        assertThat(updatedProposal.version()).isGreaterThanOrEqualTo(1L);
        assertThat(proposalAdapterRepository.findByPaymentId(persistedPayment.id())).hasSize(1);
    }

    @Test
    void should_save_allocation_candidate_with_domain_assigned_id_via_adapter_repository() {
        JpaAllocationProposalRepository proposalAdapterRepository =
                new JpaAllocationProposalRepository(springDataAllocationProposalRepository, allocationProposalEntityMapper);
        JpaAllocationProposalCandidateRepository candidateAdapterRepository =
                new JpaAllocationProposalCandidateRepository(
                        springDataAllocationProposalCandidateRepository,
                        allocationProposalCandidateEntityMapper);

        Instant now = Instant.now();
        Debtor debtor = Debtor.activeNaturalPerson(
                UUID.randomUUID(),
                "Candidate Debtor",
                "85073003328",
                now);
        DebtorEntity debtorEntity = debtorEntityMapper.toEntity(debtor);
        debtorEntity.setId(null);
        DebtorEntity savedDebtor = springDataDebtorRepository.saveAndFlush(debtorEntity);

        Debt debt = Debt.open(
                UUID.randomUUID(),
                savedDebtor.getId(),
                "DEBT-ADAPTER-CANDIDATE-1",
                new BigDecimal("120.00"),
                "EUR",
                null,
                now);
        Debt persistedDebt = new JpaDebtRepository(springDataDebtRepository, debtEntityMapper).save(debt);

        Payment payment = Payment.received(
                UUID.randomUUID(),
                "TX-ADAPTER-CANDIDATE-1",
                new BigDecimal("120.00"),
                "EUR",
                null,
                null,
                null,
                null,
                now);
        var paymentEntity = paymentEntityMapper.toEntity(payment);
        paymentEntity.setId(null);
        var savedPaymentEntity = springDataPaymentRepository.saveAndFlush(paymentEntity);
        Payment persistedPayment = paymentEntityMapper.toDomain(savedPaymentEntity);

        AllocationProposal proposal = AllocationProposal.proposed(
                UUID.randomUUID(),
                persistedPayment.id(),
                MatchingMethod.NAME,
                "candidate proposal",
                now);
        AllocationProposal persistedProposal = proposalAdapterRepository.save(proposal);

        AllocationProposalCandidate candidate = new AllocationProposalCandidate(
                UUID.randomUUID(),
                persistedProposal.id(),
                savedDebtor.getId(),
                persistedDebt.id(),
                MatchConfidence.HIGH,
                new BigDecimal("50.00"),
                0);

        AllocationProposalCandidate persistedCandidate = candidateAdapterRepository.save(candidate);

        assertThat(persistedCandidate.id()).isNotNull();
        assertThat(candidateAdapterRepository.findByProposalId(persistedProposal.id())).hasSize(1);
    }
}
