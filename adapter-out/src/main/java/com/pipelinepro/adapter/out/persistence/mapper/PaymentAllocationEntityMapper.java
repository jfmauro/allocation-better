package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.adapter.out.persistence.entity.PaymentAllocationEntity;
import com.pipelinepro.domain.PaymentAllocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentAllocationEntityMapper {

    @Mapping(target = "id", expression = "java(allocation.id())")
    @Mapping(target = "paymentId", expression = "java(allocation.paymentId())")
    @Mapping(target = "debtId", expression = "java(allocation.debtId())")
    @Mapping(target = "proposalId", expression = "java(allocation.proposalId().orElse(null))")
    @Mapping(target = "amount", expression = "java(allocation.amount())")
    @Mapping(target = "status", expression = "java(allocation.status())")
    @Mapping(target = "idempotencyKey", expression = "java(allocation.idempotencyKey())")
    @Mapping(target = "commandId", expression = "java(allocation.commandId())")
    @Mapping(target = "createdBy", expression = "java(allocation.createdBy())")
    @Mapping(target = "createdAt", expression = "java(allocation.createdAt())")
    PaymentAllocationEntity toEntity(PaymentAllocation allocation);

    default PaymentAllocation toDomain(PaymentAllocationEntity entity) {
        if (entity == null) {
            return null;
        }
        return DomainObjectReflectionFactory.paymentAllocation(
                entity.getId(),
                entity.getPaymentId(),
                entity.getDebtId(),
                entity.getProposalId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getIdempotencyKey(),
                entity.getCommandId(),
                entity.getCreatedBy(),
                entity.getCreatedAt());
    }
}
