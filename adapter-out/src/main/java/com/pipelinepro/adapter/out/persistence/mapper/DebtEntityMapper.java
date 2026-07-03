package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.adapter.out.persistence.entity.DebtEntity;
import com.pipelinepro.domain.Debt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DebtEntityMapper {

    @Mapping(target = "id", expression = "java(debt.id())")
    @Mapping(target = "debtorId", expression = "java(DomainObjectReflectionFactory.debtDebtorId(debt))")
    @Mapping(target = "reference", expression = "java(DomainObjectReflectionFactory.debtReference(debt))")
    @Mapping(target = "originalAmount", expression = "java(DomainObjectReflectionFactory.debtOriginalAmount(debt))")
    @Mapping(target = "remainingAmount", expression = "java(debt.remainingAmount())")
    @Mapping(target = "currency", expression = "java(debt.currency())")
    @Mapping(target = "status", expression = "java(debt.status())")
    @Mapping(target = "dueDate", expression = "java(debt.dueDate().orElse(null))")
    @Mapping(target = "version", expression = "java(debt.version())")
    @Mapping(target = "createdAt", expression = "java(debt.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(debt.updatedAt())")
    DebtEntity toEntity(Debt debt);

    default Debt toDomain(DebtEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Debt(
                entity.getId(),
                entity.getDebtorId(),
                entity.getReference(),
                entity.getOriginalAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getDueDate(),
                entity.getRemainingAmount(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
