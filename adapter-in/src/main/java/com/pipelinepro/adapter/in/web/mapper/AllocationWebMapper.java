package com.pipelinepro.adapter.in.web.mapper;

import com.pipelinepro.adapter.in.web.v1.dto.request.ExecuteAllocationRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationResultResponse;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.in.command.ExecuteAllocationCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface AllocationWebMapper {

    @Mapping(target = "occurredAt", source = "occurredAt")
    ExecuteAllocationCommand toExecuteAllocationCommand(ExecuteAllocationRequest request, Instant occurredAt);

    @Mapping(target = "id", expression = "java(allocation.id())")
    @Mapping(target = "paymentId", expression = "java(allocation.paymentId())")
    @Mapping(target = "debtId", expression = "java(allocation.debtId())")
    @Mapping(target = "proposalId", expression = "java(unwrap(allocation.proposalId()))")
    @Mapping(target = "amount", expression = "java(allocation.amount())")
    @Mapping(target = "status", expression = "java(allocation.status())")
    @Mapping(target = "idempotencyKey", expression = "java(allocation.idempotencyKey())")
    @Mapping(target = "commandId", expression = "java(allocation.commandId())")
    @Mapping(target = "createdBy", expression = "java(allocation.createdBy())")
    @Mapping(target = "createdAt", expression = "java(allocation.createdAt())")
    AllocationResponse toAllocationResponse(PaymentAllocation allocation);

    @Mapping(target = "allocationId", expression = "java(allocation.id())")
    @Mapping(target = "proposalId", expression = "java(unwrap(allocation.proposalId()))")
    @Mapping(target = "paymentId", expression = "java(allocation.paymentId())")
    @Mapping(target = "debtId", expression = "java(allocation.debtId())")
    @Mapping(target = "amount", expression = "java(allocation.amount())")
    @Mapping(target = "status", expression = "java(allocation.status().name())")
    @Mapping(target = "createdBy", expression = "java(allocation.createdBy())")
    @Mapping(target = "createdAt", expression = "java(allocation.createdAt())")
    AllocationResultResponse toAllocationResultResponse(PaymentAllocation allocation);

    default <T> T unwrap(Optional<T> value) {
        return value == null ? null : value.orElse(null);
    }
}
