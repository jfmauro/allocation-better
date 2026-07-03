package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.adapter.out.persistence.entity.AuditEventEntity;
import com.pipelinepro.domain.AuditEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditEventEntityMapper {

    @Mapping(target = "id", expression = "java(event.id())")
    @Mapping(target = "aggregateType", expression = "java(event.aggregateType())")
    @Mapping(target = "aggregateId", expression = "java(event.aggregateId())")
    @Mapping(target = "eventType", expression = "java(event.eventType())")
    @Mapping(target = "actor", expression = "java(event.actor())")
    @Mapping(target = "payloadJson", expression = "java(event.payloadJson())")
    @Mapping(target = "createdAt", expression = "java(event.createdAt())")
    AuditEventEntity toEntity(AuditEvent event);

    default AuditEvent toDomain(AuditEventEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AuditEvent(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getActor(),
                entity.getPayloadJson(),
                entity.getCreatedAt());
    }
}
