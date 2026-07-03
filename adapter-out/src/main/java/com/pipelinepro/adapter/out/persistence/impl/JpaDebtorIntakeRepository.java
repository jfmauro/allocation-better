package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.DebtorEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.out.DebtorIntakeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDebtorIntakeRepository implements DebtorIntakeRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaDebtorIntakeRepository.class);

    private final SpringDataDebtorRepository springDataDebtorRepository;
    private final DebtorEntityMapper debtorEntityMapper;

    public JpaDebtorIntakeRepository(
            SpringDataDebtorRepository springDataDebtorRepository,
            DebtorEntityMapper debtorEntityMapper) {
        this.springDataDebtorRepository = springDataDebtorRepository;
        this.debtorEntityMapper = debtorEntityMapper;
    }

    @Override
    public Debtor save(Debtor debtor) {
        log.info("+++start save+++");
        try {
            var entity = debtorEntityMapper.toEntity(debtor);
            entity.setId(null);
            try {
                return debtorEntityMapper.toDomain(springDataDebtorRepository.saveAndFlush(entity));
            } catch (DataIntegrityViolationException exception) {
                throw duplicateDebtorConflict(exception);
            }
        } finally {
            log.info("+++end save+++");
        }
    }

    @Override
    public boolean existsByNationalNumber(String nationalNumber) {
        log.info("+++start existsByNationalNumber+++");
        try {
            return springDataDebtorRepository.existsByNationalNumber(nationalNumber);
        } finally {
            log.info("+++end existsByNationalNumber+++");
        }
    }

    @Override
    public boolean existsByEnterpriseNumber(String enterpriseNumber) {
        log.info("+++start existsByEnterpriseNumber+++");
        try {
            return springDataDebtorRepository.existsByEnterpriseNumber(enterpriseNumber);
        } finally {
            log.info("+++end existsByEnterpriseNumber+++");
        }
    }

    private IllegalStateException duplicateDebtorConflict(DataIntegrityViolationException exception) {
        String message = extractMessage(exception).toLowerCase();
        if (message.contains("uk_debtor_national_number_hash") || message.contains("national_number_hash")) {
            return new IllegalStateException("Duplicate debtor for national number hash", exception);
        }
        if (message.contains("uk_debtor_enterprise_number") || message.contains("enterprise_number")) {
            return new IllegalStateException("Duplicate debtor for enterprise number", exception);
        }
        return new IllegalStateException("Duplicate debtor", exception);
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
