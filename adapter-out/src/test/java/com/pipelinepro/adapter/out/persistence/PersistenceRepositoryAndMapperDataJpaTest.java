package com.pipelinepro.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.pipelinepro.adapter.out.persistence.entity.AllocationProposalCandidateEntity;
import com.pipelinepro.adapter.out.persistence.entity.AllocationProposalEntity;
import com.pipelinepro.adapter.out.persistence.entity.AuditEventEntity;
import com.pipelinepro.adapter.out.persistence.entity.DebtEntity;
import com.pipelinepro.adapter.out.persistence.entity.DebtorEntity;
import com.pipelinepro.adapter.out.persistence.entity.NationalNumberAccessLogEntity;
import com.pipelinepro.adapter.out.persistence.entity.PaymentAllocationEntity;
import com.pipelinepro.adapter.out.persistence.entity.PaymentEntity;
import com.pipelinepro.adapter.out.persistence.impl.JpaAuditEventGateway;
import com.pipelinepro.adapter.out.persistence.impl.JpaNationalNumberAccessLogGateway;
import com.pipelinepro.adapter.out.persistence.mapper.AllocationProposalCandidateEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.AllocationProposalEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.AuditEventEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtorEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.NationalNumberAccessLogEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.PaymentAllocationEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.PaymentEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalCandidateRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataNationalNumberAccessLogRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentAllocationRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.NationalNumberAccessLog;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentAllocation;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PersistenceRepositoryAndMapperDataJpaTest {

    private final DebtorEntityMapper debtorMapper = Mappers.getMapper(DebtorEntityMapper.class);
    private final DebtEntityMapper debtMapper = Mappers.getMapper(DebtEntityMapper.class);
    private final PaymentEntityMapper paymentMapper = Mappers.getMapper(PaymentEntityMapper.class);
    private final AllocationProposalEntityMapper allocationProposalMapper =
            Mappers.getMapper(AllocationProposalEntityMapper.class);
    private final AllocationProposalCandidateEntityMapper allocationProposalCandidateMapper =
            Mappers.getMapper(AllocationProposalCandidateEntityMapper.class);
    private final PaymentAllocationEntityMapper paymentAllocationMapper = Mappers.getMapper(PaymentAllocationEntityMapper.class);
    private final AuditEventEntityMapper auditEventMapper = Mappers.getMapper(AuditEventEntityMapper.class);
    private final NationalNumberAccessLogEntityMapper nationalNumberAccessLogMapper =
            Mappers.getMapper(NationalNumberAccessLogEntityMapper.class);

    @Autowired
    private SpringDataDebtorRepository debtorRepository;

    @Autowired
    private SpringDataDebtRepository debtRepository;

    @Autowired
    private SpringDataPaymentRepository paymentRepository;

    @Autowired
    private SpringDataAllocationProposalRepository allocationProposalRepository;

    @Autowired
    private SpringDataAllocationProposalCandidateRepository allocationProposalCandidateRepository;

    @Autowired
    private SpringDataPaymentAllocationRepository paymentAllocationRepository;

    @Autowired
    private SpringDataAuditEventRepository auditEventRepository;

    @Autowired
    private SpringDataNationalNumberAccessLogRepository nationalNumberAccessLogRepository;

    @Test
    void should_persist_and_map_all_step5_entities() {
        Instant now = Instant.now();

        Debtor debtor = Debtor.activeNaturalPerson(
                UUID.randomUUID(),
                "Alice Debtor",
                "85073003328",
                now);
        DebtorEntity debtorEntity = debtorMapper.toEntity(debtor);
        debtorEntity.setId(null);
        DebtorEntity savedDebtor = debtorRepository.saveAndFlush(debtorEntity);
        Debtor debtorRoundTrip = debtorMapper.toDomain(savedDebtor);
        assertThat(debtorRoundTrip.id()).isNotNull();

        Debt debt = Debt.open(
                UUID.randomUUID(),
                debtorRoundTrip.id(),
                "DEBT-REF-001",
                new BigDecimal("100.00"),
                "EUR",
                null,
                now);
        DebtEntity debtEntity = debtMapper.toEntity(debt);
        debtEntity.setId(null);
        debtEntity.setVersion(null);
        DebtEntity savedDebt = debtRepository.saveAndFlush(debtEntity);
        Debt debtRoundTrip = debtMapper.toDomain(savedDebt);
        assertThat(debtRepository.findByDebtorId(debtorRoundTrip.id())).hasSize(1);
        assertThat(debtRoundTrip.id()).isNotNull();

        Payment payment = Payment.received(
                UUID.randomUUID(),
                "TX-001",
                new BigDecimal("100.00"),
                "EUR",
                "+++001/0000/00001+++",
                "free comment",
                "Alice",
                "BE**1234",
                now);
        PaymentEntity paymentEntity = paymentMapper.toEntity(payment);
        paymentEntity.setId(null);
        paymentEntity.setVersion(null);
        PaymentEntity savedPayment = paymentRepository.saveAndFlush(paymentEntity);
        Payment paymentRoundTrip = paymentMapper.toDomain(savedPayment);
        assertThat(paymentRepository.findByBankTransactionReference("TX-001")).isPresent();
        assertThat(paymentRoundTrip.id()).isNotNull();

        AllocationProposal proposal = AllocationProposal.proposed(
                UUID.randomUUID(),
                paymentRoundTrip.id(),
                MatchingMethod.NAME,
                "manual verification",
                now);
        AllocationProposalEntity proposalEntity = allocationProposalMapper.toEntity(proposal);
        proposalEntity.setId(null);
        proposalEntity.setVersion(null);
        AllocationProposalEntity savedProposal = allocationProposalRepository.saveAndFlush(proposalEntity);
        AllocationProposal proposalRoundTrip = allocationProposalMapper.toDomain(savedProposal);
        assertThat(allocationProposalRepository.findByPaymentId(paymentRoundTrip.id())).hasSize(1);
        assertThat(proposalRoundTrip.id()).isNotNull();

        AllocationProposalCandidate candidate = new AllocationProposalCandidate(
                UUID.randomUUID(),
                proposalRoundTrip.id(),
                debtorRoundTrip.id(),
                debtRoundTrip.id(),
                com.pipelinepro.domain.MatchConfidence.HIGH,
                new BigDecimal("40.00"),
                0);
        AllocationProposalCandidateEntity candidateEntity = allocationProposalCandidateMapper.toEntity(candidate);
        candidateEntity.setId(null);
        AllocationProposalCandidateEntity savedCandidate = allocationProposalCandidateRepository.saveAndFlush(candidateEntity);
        AllocationProposalCandidate candidateRoundTrip = allocationProposalCandidateMapper.toDomain(savedCandidate);
        assertThat(allocationProposalCandidateRepository.findByProposalId(proposalRoundTrip.id())).hasSize(1);
        assertThat(candidateRoundTrip.id()).isNotNull();

        PaymentAllocation paymentAllocation = PaymentAllocation.execute(
                UUID.randomUUID(),
                paymentRoundTrip,
                debtRoundTrip,
                proposalRoundTrip.id(),
                new BigDecimal("25.00"),
                "IDEMP-001",
                "CMD-001",
                "integration-test",
                now.plusSeconds(1));
        PaymentAllocationEntity paymentAllocationEntity = paymentAllocationMapper.toEntity(paymentAllocation);
        paymentAllocationEntity.setId(null);
        PaymentAllocationEntity savedPaymentAllocation = paymentAllocationRepository.saveAndFlush(paymentAllocationEntity);
        PaymentAllocation paymentAllocationRoundTrip = paymentAllocationMapper.toDomain(savedPaymentAllocation);
        assertThat(paymentAllocationRepository.findByIdempotencyKey("IDEMP-001")).isPresent();
        assertThat(paymentAllocationRoundTrip.id()).isNotNull();

        AuditEvent auditEvent = new AuditEvent(
                UUID.randomUUID(),
                "PAYMENT",
                paymentRoundTrip.id(),
                "MATCH_PROPOSED",
                "integration-test",
                "{\"ok\":true}",
                now.plusSeconds(2));
        AuditEventEntity auditEventEntity = auditEventMapper.toEntity(auditEvent);
        auditEventEntity.setId(null);
        AuditEventEntity savedAuditEvent = auditEventRepository.saveAndFlush(auditEventEntity);
        AuditEvent auditEventRoundTrip = auditEventMapper.toDomain(savedAuditEvent);
        assertThat(auditEventRoundTrip.aggregateId()).isEqualTo(paymentRoundTrip.id());

        NationalNumberAccessLog accessLog = new NationalNumberAccessLog(
                UUID.randomUUID(),
                paymentRoundTrip.id(),
                debtorRoundTrip.id(),
                "user-1",
                "manual investigation",
                now.plusSeconds(3));
        NationalNumberAccessLogEntity accessLogEntity = nationalNumberAccessLogMapper.toEntity(accessLog);
        accessLogEntity.setId(null);
        NationalNumberAccessLogEntity savedAccessLog = nationalNumberAccessLogRepository.saveAndFlush(accessLogEntity);
        NationalNumberAccessLog accessLogRoundTrip = nationalNumberAccessLogMapper.toDomain(savedAccessLog);
        assertThat(accessLogRoundTrip.optionalPaymentId()).contains(paymentRoundTrip.id());
    }

    @Test
    void should_append_audit_event_when_domain_event_has_non_null_id() {
        Instant now = Instant.now();
        JpaAuditEventGateway gateway = new JpaAuditEventGateway(auditEventRepository, auditEventMapper);

        gateway.append(new AuditEvent(
                UUID.randomUUID(),
                "PAYMENT",
                UUID.randomUUID(),
                "PAYMENT_RECEIVED",
                "integration-test",
                "{\"ok\":true}",
                now));

        assertThat(auditEventRepository.count()).isEqualTo(1L);
        assertThat(auditEventRepository.findAll().getFirst().getId()).isNotNull();
    }

    @Test
    void should_log_national_number_access_when_domain_log_has_non_null_id() {
        Instant now = Instant.now();

        Debtor debtor = Debtor.activeNaturalPerson(
                UUID.randomUUID(),
                "Debtor For Access Log",
                "85073003328",
                now);
        DebtorEntity debtorEntity = debtorMapper.toEntity(debtor);
        debtorEntity.setId(null);
        DebtorEntity savedDebtor = debtorRepository.saveAndFlush(debtorEntity);

        JpaNationalNumberAccessLogGateway gateway =
                new JpaNationalNumberAccessLogGateway(nationalNumberAccessLogRepository, nationalNumberAccessLogMapper);
        gateway.logAccess(new NationalNumberAccessLog(
                UUID.randomUUID(),
                null,
                savedDebtor.getId(),
                "user-2",
                "test-access",
                now.plusSeconds(1)));

        assertThat(nationalNumberAccessLogRepository.count()).isEqualTo(1L);
        assertThat(nationalNumberAccessLogRepository.findAll().getFirst().getId()).isNotNull();
    }
}
