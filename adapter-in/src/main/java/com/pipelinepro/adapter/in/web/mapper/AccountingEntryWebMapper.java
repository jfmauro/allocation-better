package com.pipelinepro.adapter.in.web.mapper;

import com.pipelinepro.adapter.in.web.v1.dto.response.AccountingEntryResponse;
import com.pipelinepro.domain.AccountingEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountingEntryWebMapper {

    @Mapping(target = "eventType", expression = "java(accountingEntry.eventType().name())")
    @Mapping(target = "sourceAggregateType", expression = "java(accountingEntry.sourceAggregateType().name())")
    @Mapping(target = "sourceAggregateId", expression = "java(accountingEntry.sourceAggregateId())")
    @Mapping(target = "amount", expression = "java(accountingEntry.amount())")
    @Mapping(target = "currency", expression = "java(accountingEntry.currency())")
    @Mapping(target = "occurredAt", expression = "java(accountingEntry.occurredAt())")
    @Mapping(target = "createdAt", expression = "java(accountingEntry.createdAt())")
    AccountingEntryResponse toAccountingEntryResponse(AccountingEntry accountingEntry);
}
