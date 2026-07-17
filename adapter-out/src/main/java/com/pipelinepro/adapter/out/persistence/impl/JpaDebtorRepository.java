package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.DebtorEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.out.DebtorRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDebtorRepository implements DebtorRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaDebtorRepository.class);

    private final SpringDataDebtorRepository springDataDebtorRepository;
    private final DebtorEntityMapper debtorEntityMapper;

    public JpaDebtorRepository(
            SpringDataDebtorRepository springDataDebtorRepository,
            DebtorEntityMapper debtorEntityMapper) {
        this.springDataDebtorRepository = springDataDebtorRepository;
        this.debtorEntityMapper = debtorEntityMapper;
    }

    @Override
    public Optional<Debtor> findById(UUID debtorId) {
        log.info("+++start findById+++");
        try {
            return springDataDebtorRepository.findById(debtorId).map(debtorEntityMapper::toDomain);
        } finally {
            log.info("+++end findById+++");
        }
    }

    @Override
    public Optional<Debtor> findByNationalNumber(String nationalNumber) {
        log.info("+++start findByNationalNumber+++");
        try {
            return springDataDebtorRepository.findByNationalNumber(nationalNumber)
                    .map(debtorEntityMapper::toDomain);
        } finally {
            log.info("+++end findByNationalNumber+++");
        }
    }

    @Override
    public Optional<Debtor> findByEnterpriseNumber(String enterpriseNumber) {
        log.info("+++start findByEnterpriseNumber+++");
        try {
            return springDataDebtorRepository.findByEnterpriseNumber(enterpriseNumber)
                    .map(debtorEntityMapper::toDomain);
        } finally {
            log.info("+++end findByEnterpriseNumber+++");
        }
    }

    @Override
    public List<Debtor> findByIds(Set<UUID> debtorIds) {
        log.info("+++start findByIds+++");
        try {
            if (debtorIds == null || debtorIds.isEmpty()) {
                return List.of();
            }
            return springDataDebtorRepository.findAllById(debtorIds).stream()
                    .map(debtorEntityMapper::toDomain)
                    .toList();
        } finally {
            log.info("+++end findByIds+++");
        }
    }

    @Override
    public List<Debtor> findAllActive() {
        log.info("+++start findAllActive+++");
        try {
            return springDataDebtorRepository.findByActiveTrue().stream()
                    .map(debtorEntityMapper::toDomain)
                    .toList();
        } finally {
            log.info("+++end findAllActive+++");
        }
    }

    @Override
    public List<Debtor> findAll() {
        log.info("+++start findAll+++");
        try {
            return springDataDebtorRepository.findAll().stream().map(debtorEntityMapper::toDomain).toList();
        } finally {
            log.info("+++end findAll+++");
        }
    }
}
