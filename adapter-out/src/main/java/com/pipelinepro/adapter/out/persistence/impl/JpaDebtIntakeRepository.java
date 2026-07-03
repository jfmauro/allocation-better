package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.DebtEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.port.out.DebtIntakeRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDebtIntakeRepository implements DebtIntakeRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaDebtIntakeRepository.class);

    private final SpringDataDebtRepository springDataDebtRepository;
    private final SpringDataDebtorRepository springDataDebtorRepository;
    private final DebtEntityMapper debtEntityMapper;

    public JpaDebtIntakeRepository(
            SpringDataDebtRepository springDataDebtRepository,
            SpringDataDebtorRepository springDataDebtorRepository,
            DebtEntityMapper debtEntityMapper) {
        this.springDataDebtRepository = springDataDebtRepository;
        this.springDataDebtorRepository = springDataDebtorRepository;
        this.debtEntityMapper = debtEntityMapper;
    }

    @Override
    public Debt save(Debt debt) {
        log.info("+++start save+++");
        try {
            var entity = debtEntityMapper.toEntity(debt);
            entity.setId(null);
            entity.setVersion(null);
            String reference = entity.getReference();
            try {
                return debtEntityMapper.toDomain(springDataDebtRepository.saveAndFlush(entity));
            } catch (DataIntegrityViolationException exception) {
                throw duplicateDebtReferenceConflict(reference, exception);
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (debt.version() != 0L) {
                    throw ex;
                }
                entity.setId(null);
                entity.setVersion(null);
                try {
                    return debtEntityMapper.toDomain(springDataDebtRepository.saveAndFlush(entity));
                } catch (DataIntegrityViolationException exception) {
                    throw duplicateDebtReferenceConflict(reference, exception);
                }
            }
        } finally {
            log.info("+++end save+++");
        }
    }

    @Override
    public boolean existsByReference(String reference) {
        log.info("+++start existsByReference+++");
        try {
            return springDataDebtRepository.existsByReference(reference);
        } finally {
            log.info("+++end existsByReference+++");
        }
    }

    @Override
    public boolean debtorExists(UUID debtorId) {
        log.info("+++start debtorExists+++");
        try {
            return springDataDebtorRepository.existsById(debtorId);
        } finally {
            log.info("+++end debtorExists+++");
        }
    }

    private IllegalStateException duplicateDebtReferenceConflict(String reference, DataIntegrityViolationException exception) {
        String message = extractMessage(exception).toLowerCase();
        if (message.contains("uk_debt_reference_global") || message.contains("reference")) {
            return new IllegalStateException("Duplicate debt reference: " + reference, exception);
        }
        throw exception;
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
}
