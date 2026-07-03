package com.pipelinepro.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pipelinepro.adapter.out.persistence.entity.DebtEntity;
import com.pipelinepro.adapter.out.persistence.entity.DebtorEntity;
import com.pipelinepro.adapter.out.persistence.entity.IntakeRequestEntity;
import com.pipelinepro.adapter.out.persistence.entity.PaymentAllocationEntity;
import com.pipelinepro.adapter.out.persistence.entity.PaymentEntity;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataIntakeRequestRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentAllocationRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import com.pipelinepro.domain.AllocationStatus;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.DebtorType;
import com.pipelinepro.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
class PersistenceConstraintsDataJpaTest {

    @Autowired
    private SpringDataPaymentRepository paymentRepository;

    @Autowired
    private SpringDataDebtorRepository debtorRepository;

    @Autowired
    private SpringDataDebtRepository debtRepository;

    @Autowired
    private SpringDataPaymentAllocationRepository paymentAllocationRepository;

    @Autowired
    private SpringDataIntakeRequestRepository intakeRequestRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_enforce_unique_payment_bank_transaction_reference() {
        paymentRepository.saveAndFlush(validPaymentEntity("TX-UNIQUE-1"));

        assertThatThrownBy(() -> paymentRepository.saveAndFlush(validPaymentEntity("TX-UNIQUE-1")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_enforce_not_null_payment_amount() {
        PaymentEntity invalid = validPaymentEntity("TX-NOTNULL-1");
        invalid.setAmount(null);

        assertThatThrownBy(() -> paymentRepository.saveAndFlush(invalid))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_enforce_unique_payment_allocation_idempotency_key() {
        DebtorEntity debtor = debtorRepository.saveAndFlush(validDebtorEntity());
        DebtEntity debt = debtRepository.saveAndFlush(validDebtEntity(debtor.getId()));
        PaymentEntity payment = paymentRepository.saveAndFlush(validPaymentEntity("TX-IDEMP-1"));

        paymentAllocationRepository.saveAndFlush(validPaymentAllocationEntity(payment.getId(), debt.getId(), "IDEMP-KEY-1", "CMD-1"));

        assertThatThrownBy(() -> paymentAllocationRepository
                .saveAndFlush(validPaymentAllocationEntity(payment.getId(), debt.getId(), "IDEMP-KEY-1", "CMD-2")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_enforce_unique_debt_reference_globally() {
        DebtorEntity debtor1 = debtorRepository.saveAndFlush(validDebtorEntity("hash-constraint-1"));
        DebtorEntity debtor2 = debtorRepository.saveAndFlush(validDebtorEntity("hash-constraint-2"));

        debtRepository.saveAndFlush(validDebtEntity(debtor1.getId(), "DEBT-GLOBAL-1"));

        assertThatThrownBy(() -> debtRepository.saveAndFlush(validDebtEntity(debtor2.getId(), "DEBT-GLOBAL-1")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_enforce_unique_intake_idempotency_key() {
        intakeRequestRepository.saveAndFlush(validIntakeRequestEntity("IDEMP-INTAKE-1", "CREATE_DEBTOR"));

        assertThatThrownBy(() -> intakeRequestRepository.saveAndFlush(validIntakeRequestEntity("IDEMP-INTAKE-1", "CREATE_DEBT")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_generate_global_debt_reference_unique_constraint_with_jpa_schema_generation() {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.table_constraints
                where table_name = 'DEBT'
                  and constraint_name = 'UK_DEBT_REFERENCE_GLOBAL'
                """,
                Integer.class);

        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
    }

    private PaymentEntity validPaymentEntity(String reference) {
        PaymentEntity entity = new PaymentEntity();
        entity.setBankTransactionReference(reference);
        entity.setAmount(new BigDecimal("150.00"));
        entity.setRemainingAmount(new BigDecimal("150.00"));
        entity.setCurrency("EUR");
        entity.setStatus(PaymentStatus.RECEIVED);
        entity.setStructuredCommunication("+++001/0000/00001+++");
        entity.setFreeCommunication("free text");
        entity.setPayerName("Test Payer");
        entity.setPayerIbanMasked("BE**0000");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private DebtorEntity validDebtorEntity() {
        return validDebtorEntity("hash-constraint");
    }

    private DebtorEntity validDebtorEntity(String nationalNumber) {
        DebtorEntity entity = new DebtorEntity();
        entity.setType(DebtorType.NATURAL_PERSON);
        entity.setDisplayName("Constraint Debtor");
        entity.setNationalNumber(nationalNumber);
        entity.setActive(true);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private DebtEntity validDebtEntity(UUID debtorId) {
        return validDebtEntity(debtorId, "DEBT-CONSTRAINT-1");
    }

    private DebtEntity validDebtEntity(UUID debtorId, String reference) {
        DebtEntity entity = new DebtEntity();
        entity.setDebtorId(debtorId);
        entity.setReference(reference);
        entity.setOriginalAmount(new BigDecimal("150.00"));
        entity.setRemainingAmount(new BigDecimal("150.00"));
        entity.setCurrency("EUR");
        entity.setStatus(DebtStatus.OPEN);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private PaymentAllocationEntity validPaymentAllocationEntity(
            UUID paymentId,
            UUID debtId,
            String idempotencyKey,
            String commandId) {
        PaymentAllocationEntity entity = new PaymentAllocationEntity();
        entity.setPaymentId(paymentId);
        entity.setDebtId(debtId);
        entity.setAmount(new BigDecimal("25.00"));
        entity.setStatus(allocatedStatus());
        entity.setIdempotencyKey(idempotencyKey);
        entity.setCommandId(commandId);
        entity.setCreatedBy("constraint-test");
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private IntakeRequestEntity validIntakeRequestEntity(String idempotencyKey, String operation) {
        IntakeRequestEntity entity = new IntakeRequestEntity();
        entity.setIdempotencyKey(idempotencyKey);
        entity.setOperation(operation);
        entity.setCorrelationId("corr-constraint");
        entity.setStatus("REQUESTED");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private AllocationStatus allocatedStatus() {
        try {
            return AllocationStatus.valueOf("ALLOCATED");
        } catch (IllegalArgumentException ignored) {
            return AllocationStatus.valueOf("EFFECTIVE");
        }
    }
}
