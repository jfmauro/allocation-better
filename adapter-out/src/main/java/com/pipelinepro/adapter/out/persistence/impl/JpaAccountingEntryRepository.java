package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.entity.AccountingEntryEntity;
import com.pipelinepro.adapter.out.persistence.mapper.AccountingEntryEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAccountingEntryRepository;
import com.pipelinepro.domain.AccountingEntry;
import com.pipelinepro.domain.AccountingEventType;
import com.pipelinepro.domain.port.out.AccountingEntryRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAccountingEntryRepository implements AccountingEntryRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaAccountingEntryRepository.class);

    private final SpringDataAccountingEntryRepository springDataAccountingEntryRepository;
    private final AccountingEntryEntityMapper accountingEntryEntityMapper;

    public JpaAccountingEntryRepository(
            SpringDataAccountingEntryRepository springDataAccountingEntryRepository,
            AccountingEntryEntityMapper accountingEntryEntityMapper) {
        this.springDataAccountingEntryRepository = springDataAccountingEntryRepository;
        this.accountingEntryEntityMapper = accountingEntryEntityMapper;
    }

    @Override
    public AccountingEntry append(AccountingEntry accountingEntry) {
        log.info("+++start append+++");
        try {
            AccountingEntryEntity entity = accountingEntryEntityMapper.toEntity(accountingEntry);
            entity.setId(null);
            entity.setVersion(null);
            return accountingEntryEntityMapper.toDomain(springDataAccountingEntryRepository.saveAndFlush(entity));
        } finally {
            log.info("+++end append+++");
        }
    }

    @Override
    public List<AccountingEntry> findByCriteria(
            Optional<AccountingEventType> eventType,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate) {
        log.info("+++start findByCriteria+++");
        try {
            Specification<AccountingEntryEntity> specification = Specification.where(hasEventType(eventType))
                    .and(occurredOnOrAfter(fromDate))
                    .and(occurredOnOrBefore(toDate));
            return springDataAccountingEntryRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "occurredAt"))
                    .stream()
                    .map(accountingEntryEntityMapper::toDomain)
                    .toList();
        } finally {
            log.info("+++end findByCriteria+++");
        }
    }

    private Specification<AccountingEntryEntity> hasEventType(Optional<AccountingEventType> eventType) {
        return (root, query, cb) -> eventType.map(type -> cb.equal(root.get("eventType"), type)).orElse(null);
    }

    private Specification<AccountingEntryEntity> occurredOnOrAfter(Optional<LocalDate> fromDate) {
        return (root, query, cb) -> fromDate.map(date -> cb.greaterThanOrEqualTo(
                root.get("occurredAt"),
                date.atStartOfDay().toInstant(ZoneOffset.UTC))).orElse(null);
    }

    private Specification<AccountingEntryEntity> occurredOnOrBefore(Optional<LocalDate> toDate) {
        return (root, query, cb) -> toDate.map(date -> cb.lessThan(
                root.get("occurredAt"),
                date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))).orElse(null);
    }
}
