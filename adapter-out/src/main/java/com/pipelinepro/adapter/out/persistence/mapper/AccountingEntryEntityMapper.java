package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.adapter.out.persistence.entity.AccountingEntryEntity;
import com.pipelinepro.domain.AccountingEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountingEntryEntityMapper {

    @Mapping(target = "id", expression = "java(accountingEntry.id())")
    @Mapping(target = "eventType", expression = "java(accountingEntry.eventType())")
    @Mapping(target = "sourceAggregateType", expression = "java(accountingEntry.sourceAggregateType())")
    @Mapping(target = "sourceAggregateId", expression = "java(accountingEntry.sourceAggregateId())")
    @Mapping(target = "amount", expression = "java(accountingEntry.amount())")
    @Mapping(target = "currency", expression = "java(accountingEntry.currency())")
    @Mapping(target = "occurredAt", expression = "java(accountingEntry.occurredAt())")
    @Mapping(target = "createdAt", expression = "java(accountingEntry.createdAt())")
    AccountingEntryEntity toEntity(AccountingEntry accountingEntry);

    default AccountingEntry toDomain(AccountingEntryEntity entity) {
        if (entity == null) {
            return null;
        }
        return AccountingEntry.append(
                entity.getId(),
                entity.getEventType(),
                entity.getSourceAggregateType(),
                entity.getSourceAggregateId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getOccurredAt(),
                entity.getCreatedAt());
    }
}
