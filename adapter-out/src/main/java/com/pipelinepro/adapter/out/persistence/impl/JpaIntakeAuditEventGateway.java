package com.pipelinepro.adapter.out.persistence.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.pipelinepro.adapter.out.persistence.entity.AuditEventEntity;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.domain.port.out.IntakeAuditEventGateway;
import com.pipelinepro.domain.port.out.command.PublishIntakeAuditEventCommand;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class JpaIntakeAuditEventGateway implements IntakeAuditEventGateway {

    private static final Logger log = LoggerFactory.getLogger(JpaIntakeAuditEventGateway.class);

    private final SpringDataAuditEventRepository springDataAuditEventRepository;
    private final ObjectMapper objectMapper;

    public JpaIntakeAuditEventGateway(SpringDataAuditEventRepository springDataAuditEventRepository) {
        this.springDataAuditEventRepository = springDataAuditEventRepository;
        this.objectMapper = JsonMapper.builder().build();
    }

    @Override
    public void publish(PublishIntakeAuditEventCommand command) {
        log.info("+++start publish+++");
        try {
            AuditEventEntity entity = new AuditEventEntity();
            entity.setId(null);
            entity.setAggregateType(command.aggregateType().name());
            entity.setAggregateId(command.aggregateId());
            entity.setEventType(command.eventType());
            entity.setActor("system");
            entity.setCorrelationId(command.correlationId());
            entity.setPayloadJson(toPayload(command));
            entity.setCreatedAt(command.occurredAt());
            springDataAuditEventRepository.save(entity);
        } finally {
            log.info("+++end publish+++");
        }
    }

    private String toPayload(PublishIntakeAuditEventCommand command) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("aggregateType", command.aggregateType().name());
        payload.put("lifecycle", command.lifecycle().name());
        payload.put("reasonCode", command.reasonCode());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize intake audit payload", exception);
        }
    }
}
