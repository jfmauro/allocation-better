package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.adapter.out.persistence.entity.DebtorEntity;
import com.pipelinepro.domain.Debtor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DebtorEntityMapper {

    @Mapping(target = "id", expression = "java(debtor.id())")
    @Mapping(target = "type", expression = "java(debtor.type())")
    @Mapping(target = "displayName", expression = "java(debtor.displayName())")
    @Mapping(target = "nationalNumber", expression = "java(debtor.nationalNumber().orElse(null))")
    @Mapping(target = "enterpriseNumber", expression = "java(debtor.enterpriseNumber().orElse(null))")
    @Mapping(target = "active", expression = "java(debtor.active())")
    @Mapping(target = "createdAt", expression = "java(debtor.createdAt())")
    DebtorEntity toEntity(Debtor debtor);

    default Debtor toDomain(DebtorEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Debtor(
                entity.getId(),
                entity.getType(),
                entity.getDisplayName(),
                entity.getNationalNumber(),
                entity.getEnterpriseNumber(),
                entity.isActive(),
                entity.getCreatedAt());
    }
}
