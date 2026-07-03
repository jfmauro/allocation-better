package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.AuditEventEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.port.out.AuditEventGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAuditEventGateway implements AuditEventGateway {

    private static final Logger log = LoggerFactory.getLogger(JpaAuditEventGateway.class);

    private final SpringDataAuditEventRepository springDataAuditEventRepository;
    private final AuditEventEntityMapper auditEventEntityMapper;

    public JpaAuditEventGateway(
            SpringDataAuditEventRepository springDataAuditEventRepository,
            AuditEventEntityMapper auditEventEntityMapper) {
        this.springDataAuditEventRepository = springDataAuditEventRepository;
        this.auditEventEntityMapper = auditEventEntityMapper;
    }

    @Override
    public void append(AuditEvent event) {
        log.info("+++start append+++");
        try {
            var auditEventEntity = auditEventEntityMapper.toEntity(event);
            auditEventEntity.setId(null);
            springDataAuditEventRepository.save(auditEventEntity);
        } finally {
            log.info("+++end append+++");
        }
    }
}
