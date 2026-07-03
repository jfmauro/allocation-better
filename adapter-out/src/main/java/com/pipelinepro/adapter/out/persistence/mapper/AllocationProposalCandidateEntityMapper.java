package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.adapter.out.persistence.entity.AllocationProposalCandidateEntity;
import com.pipelinepro.domain.AllocationProposalCandidate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AllocationProposalCandidateEntityMapper {

    @Mapping(target = "id", expression = "java(candidate.id())")
    @Mapping(target = "proposalId", expression = "java(candidate.proposalId())")
    @Mapping(target = "debtorId", expression = "java(candidate.debtorId())")
    @Mapping(target = "debtId", expression = "java(candidate.debtId())")
    @Mapping(target = "confidence", expression = "java(candidate.confidence())")
    @Mapping(target = "suggestedAmount", expression = "java(candidate.suggestedAmount())")
    @Mapping(target = "rankOrder", expression = "java(candidate.rankOrder())")
    AllocationProposalCandidateEntity toEntity(AllocationProposalCandidate candidate);

    default AllocationProposalCandidate toDomain(AllocationProposalCandidateEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AllocationProposalCandidate(
                entity.getId(),
                entity.getProposalId(),
                entity.getDebtorId(),
                entity.getDebtId(),
                entity.getConfidence(),
                entity.getSuggestedAmount(),
                entity.getRankOrder());
    }
}
