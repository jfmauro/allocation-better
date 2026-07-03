package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.adapter.out.persistence.entity.AllocationProposalEntity;
import com.pipelinepro.domain.AllocationProposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AllocationProposalEntityMapper {

    @Mapping(target = "id", expression = "java(proposal.id())")
    @Mapping(target = "paymentId", expression = "java(proposal.paymentId())")
    @Mapping(target = "status", expression = "java(proposal.status())")
    @Mapping(target = "matchingMethod", expression = "java(proposal.matchingMethod())")
    @Mapping(target = "reason", expression = "java(proposal.reason().orElse(null))")
    @Mapping(target = "validatedBy", expression = "java(proposal.validatedBy().orElse(null))")
    @Mapping(target = "validatedAt", expression = "java(proposal.validatedAt().orElse(null))")
    @Mapping(target = "version", expression = "java(proposal.version())")
    @Mapping(target = "createdAt", expression = "java(proposal.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(proposal.updatedAt())")
    AllocationProposalEntity toEntity(AllocationProposal proposal);

    default AllocationProposal toDomain(AllocationProposalEntity entity) {
        if (entity == null) {
            return null;
        }
        return DomainObjectReflectionFactory.allocationProposal(
                entity.getId(),
                entity.getPaymentId(),
                entity.getStatus(),
                entity.getMatchingMethod(),
                entity.getReason(),
                entity.getValidatedBy(),
                entity.getValidatedAt(),
                null,
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
