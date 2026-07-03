package com.pipelinepro.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pipelinepro.adapter.out.persistence.impl.JpaDebtIntakeRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaDebtorIntakeRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaIntakeAuditEventGateway;
import com.pipelinepro.adapter.out.persistence.mapper.DebtEntityMapper;
import com.pipelinepro.adapter.out.persistence.mapper.DebtorEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.out.IntakeAggregateType;
import com.pipelinepro.domain.port.out.IntakeAuditLifecycle;
import com.pipelinepro.domain.port.out.command.PublishIntakeAuditEventCommand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class IntakeAdapterDataJpaTest {

    private final DebtorEntityMapper debtorEntityMapper = Mappers.getMapper(DebtorEntityMapper.class);
    private final DebtEntityMapper debtEntityMapper = Mappers.getMapper(DebtEntityMapper.class);

    @Autowired
    private SpringDataDebtorRepository springDataDebtorRepository;

    @Autowired
    private SpringDataDebtRepository springDataDebtRepository;

    @Autowired
    private SpringDataAuditEventRepository springDataAuditEventRepository;

    @Test
    void should_map_duplicate_debtor_conflict_to_illegal_state() {
        JpaDebtorIntakeRepository repository = new JpaDebtorIntakeRepository(springDataDebtorRepository, debtorEntityMapper);
        Instant now = Instant.now();

        repository.save(Debtor.activeNaturalPerson(
                UUID.randomUUID(),
                "Debtor A",
                "85073003328",
                now));

        assertThatThrownBy(() -> repository.save(Debtor.activeNaturalPerson(
                        UUID.randomUUID(),
                        "Debtor B",
                        "85073003328",
                        now.plusSeconds(1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate debtor");
    }

    @Test
    void should_map_duplicate_debt_reference_conflict_to_illegal_state() {
        JpaDebtorIntakeRepository debtorRepository = new JpaDebtorIntakeRepository(springDataDebtorRepository, debtorEntityMapper);
        JpaDebtIntakeRepository debtRepository =
                new JpaDebtIntakeRepository(springDataDebtRepository, springDataDebtorRepository, debtEntityMapper);
        Instant now = Instant.now();
        UUID debtorId = debtorRepository.save(Debtor.activeNaturalPerson(
                        UUID.randomUUID(),
                        "Debtor A",
                        "85073003328",
                        now))
                .id();

        debtRepository.save(new Debt(
                UUID.randomUUID(),
                debtorId,
                "DEBT-DUP-REF-ADAPTER-1",
                new BigDecimal("50.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                new BigDecimal("50.00"),
                0L,
                now,
                now));

        assertThatThrownBy(() -> debtRepository.save(new Debt(
                        UUID.randomUUID(),
                        debtorId,
                        "DEBT-DUP-REF-ADAPTER-1",
                        new BigDecimal("50.00"),
                        "EUR",
                        DebtStatus.OPEN,
                        null,
                        new BigDecimal("50.00"),
                        0L,
                        now.plusSeconds(1),
                        now.plusSeconds(1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate debt reference");
    }

    @Test
    void should_report_debtor_existence_for_debt_intake() {
        JpaDebtorIntakeRepository debtorRepository = new JpaDebtorIntakeRepository(springDataDebtorRepository, debtorEntityMapper);
        JpaDebtIntakeRepository debtRepository =
                new JpaDebtIntakeRepository(springDataDebtRepository, springDataDebtorRepository, debtEntityMapper);
        UUID debtorId = debtorRepository.save(Debtor.activeNaturalPerson(
                        UUID.randomUUID(),
                        "Debtor A",
                        "85073003328",
                        Instant.now()))
                .id();

        assertThat(debtRepository.debtorExists(debtorId)).isTrue();
        assertThat(debtRepository.debtorExists(UUID.randomUUID())).isFalse();
    }

    @Test
    void should_persist_intake_audit_event_from_domain_command() {
        JpaIntakeAuditEventGateway gateway = new JpaIntakeAuditEventGateway(springDataAuditEventRepository);
        UUID aggregateId = UUID.randomUUID();
        gateway.publish(new PublishIntakeAuditEventCommand(
                UUID.randomUUID(),
                IntakeAggregateType.DEBT,
                aggregateId,
                IntakeAuditLifecycle.REJECTED,
                "DUPLICATE",
                "corr-audit-1",
                Instant.now()));

        assertThat(springDataAuditEventRepository.count()).isEqualTo(1L);
        assertThat(springDataAuditEventRepository.findAll().getFirst().getAggregateId()).isEqualTo(aggregateId);
        assertThat(springDataAuditEventRepository.findAll().getFirst().getEventType()).isEqualTo("DEBT_REJECTED");
        assertThat(springDataAuditEventRepository.findAll().getFirst().getCorrelationId()).isEqualTo("corr-audit-1");
    }
}
