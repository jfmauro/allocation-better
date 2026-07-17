package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.DebtEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.port.out.DebtRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDebtRepository implements DebtRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaDebtRepository.class);

    private final SpringDataDebtRepository springDataDebtRepository;
    private final DebtEntityMapper debtEntityMapper;

    public JpaDebtRepository(
            SpringDataDebtRepository springDataDebtRepository,
            DebtEntityMapper debtEntityMapper) {
        this.springDataDebtRepository = springDataDebtRepository;
        this.debtEntityMapper = debtEntityMapper;
    }

    @Override
    public Debt save(Debt debt) {
        log.info("+++start save+++");
        try {
            var entity = debtEntityMapper.toEntity(debt);
            entity.markNotNew();
            try {
                return debtEntityMapper.toDomain(springDataDebtRepository.saveAndFlush(entity));
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (debt.version() != 0L) {
                    throw ex;
                }
                entity.setId(null);
                entity.setVersion(null);
                return debtEntityMapper.toDomain(springDataDebtRepository.saveAndFlush(entity));
            }
        } finally {
            log.info("+++end save+++");
        }
    }

    @Override
    public Optional<Debt> findById(UUID debtId) {
        log.info("+++start findById+++");
        try {
            return springDataDebtRepository.findById(debtId).map(debtEntityMapper::toDomain);
        } finally {
            log.info("+++end findById+++");
        }
    }

    @Override
    public List<Debt> findByIds(Set<UUID> debtIds) {
        log.info("+++start findByIds+++");
        try {
            if (debtIds == null || debtIds.isEmpty()) {
                return List.of();
            }
            return springDataDebtRepository.findAllById(debtIds).stream().map(debtEntityMapper::toDomain).toList();
        } finally {
            log.info("+++end findByIds+++");
        }
    }

    @Override
    public List<Debt> findByDebtorId(UUID debtorId) {
        log.info("+++start findByDebtorId+++");
        try {
            return springDataDebtRepository.findByDebtorId(debtorId).stream().map(debtEntityMapper::toDomain).toList();
        } finally {
            log.info("+++end findByDebtorId+++");
        }
    }

    @Override
    public List<Debt> findByDebtorIds(Set<UUID> debtorIds) {
        log.info("+++start findByDebtorIds+++");
        try {
            if (debtorIds == null || debtorIds.isEmpty()) {
                return List.of();
            }
            return springDataDebtRepository.findByDebtorIdIn(debtorIds).stream()
                    .map(debtEntityMapper::toDomain)
                    .toList();
        } finally {
            log.info("+++end findByDebtorIds+++");
        }
    }

    @Override
    public List<Debt> findByReference(String reference) {
        log.info("+++start findByReference+++");
        try {
            return springDataDebtRepository.findByReference(reference).stream()
                    .map(debtEntityMapper::toDomain)
                    .toList();
        } finally {
            log.info("+++end findByReference+++");
        }
    }
}
