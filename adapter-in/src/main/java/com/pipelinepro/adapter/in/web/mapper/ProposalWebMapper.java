package com.pipelinepro.adapter.in.web.mapper;

import com.pipelinepro.adapter.in.web.v1.dto.request.MarkUnmatchedRequest;
import com.pipelinepro.adapter.in.web.v1.dto.request.RejectProposalRequest;
import com.pipelinepro.adapter.in.web.v1.dto.request.RequestInvestigationRequest;
import com.pipelinepro.adapter.in.web.v1.dto.request.SelectDebtRequest;
import com.pipelinepro.adapter.in.web.v1.dto.request.ValidateProposalRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalListResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalCandidateResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalSummaryResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationResultResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.ProposalCandidateDebtResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.ProposalCandidateDebtorResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.ProposalStateResponse;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.in.command.MarkUnmatchedCommand;
import com.pipelinepro.domain.port.in.command.RejectProposalCommand;
import com.pipelinepro.domain.port.in.command.RequestInvestigationCommand;
import com.pipelinepro.domain.port.in.command.SelectDebtCommand;
import com.pipelinepro.domain.port.in.command.ValidateProposalCommand;
import com.pipelinepro.domain.value.StructuredCommunication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProposalWebMapper {

    @Mapping(target = "proposalId", source = "proposalId")
    @Mapping(target = "debtId", source = "request.debtId")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "actor", source = "request.actor")
    @Mapping(target = "reason", source = "request.reason")
    @Mapping(target = "occurredAt", source = "occurredAt")
    ValidateProposalCommand toValidateProposalCommand(UUID proposalId, ValidateProposalRequest request, Instant occurredAt);

    @Mapping(target = "proposalId", source = "proposalId")
    @Mapping(target = "actor", source = "request.actor")
    @Mapping(target = "reason", source = "request.reason")
    @Mapping(target = "occurredAt", source = "occurredAt")
    RejectProposalCommand toRejectProposalCommand(UUID proposalId, RejectProposalRequest request, Instant occurredAt);

    @Mapping(target = "proposalId", source = "proposalId")
    @Mapping(target = "debtId", source = "request.debtId")
    @Mapping(target = "actor", source = "request.actor")
    @Mapping(target = "occurredAt", source = "occurredAt")
    SelectDebtCommand toSelectDebtCommand(UUID proposalId, SelectDebtRequest request, Instant occurredAt);

    @Mapping(target = "proposalId", source = "proposalId")
    @Mapping(target = "actor", source = "request.actor")
    @Mapping(target = "reason", source = "request.reason")
    @Mapping(target = "occurredAt", source = "occurredAt")
    MarkUnmatchedCommand toMarkUnmatchedCommand(UUID proposalId, MarkUnmatchedRequest request, Instant occurredAt);

    @Mapping(target = "proposalId", source = "proposalId")
    @Mapping(target = "actor", source = "request.actor")
    @Mapping(target = "reason", source = "request.reason")
    @Mapping(target = "occurredAt", source = "occurredAt")
    RequestInvestigationCommand toRequestInvestigationCommand(
            UUID proposalId,
            RequestInvestigationRequest request,
            Instant occurredAt);

    @Mapping(target = "id", expression = "java(proposal.id())")
    @Mapping(target = "status", expression = "java(proposal.status())")
    @Mapping(target = "matchingMethod", expression = "java(proposal.matchingMethod())")
    @Mapping(target = "reason", expression = "java(unwrap(proposal.reason()))")
    @Mapping(target = "createdAt", expression = "java(proposal.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(proposal.updatedAt())")
    AllocationProposalSummaryResponse toAllocationProposalSummaryResponse(AllocationProposal proposal);

    @Mapping(target = "id", expression = "java(proposal.id())")
    @Mapping(target = "paymentId", expression = "java(proposal.paymentId())")
    @Mapping(target = "status", expression = "java(proposal.status())")
    @Mapping(target = "matchingMethod", expression = "java(proposal.matchingMethod())")
    @Mapping(target = "reason", expression = "java(unwrap(proposal.reason()))")
    @Mapping(target = "validatedBy", expression = "java(unwrap(proposal.validatedBy()))")
    @Mapping(target = "validatedAt", expression = "java(unwrap(proposal.validatedAt()))")
    @Mapping(target = "selectedDebtId", expression = "java(unwrap(proposal.selectedDebtId()))")
    @Mapping(target = "candidates", ignore = true)
    @Mapping(target = "version", expression = "java(proposal.version())")
    @Mapping(target = "createdAt", expression = "java(proposal.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(proposal.updatedAt())")
    AllocationProposalResponse toAllocationProposalResponseWithoutCandidates(AllocationProposal proposal);

    default AllocationProposalResponse toAllocationProposalResponse(
            AllocationProposal proposal,
            List<AllocationProposalCandidate> candidates) {
        List<AllocationProposalCandidateResponse> candidateResponses = candidates == null
                ? List.of()
                : candidates.stream().map(this::toAllocationProposalCandidateResponse).toList();
        return toAllocationProposalResponseWithCandidateResponses(proposal, candidateResponses);
    }

    default AllocationProposalResponse toAllocationProposalResponseWithCandidateResponses(
            AllocationProposal proposal,
            List<AllocationProposalCandidateResponse> candidates) {
        AllocationProposalResponse base = toAllocationProposalResponseWithoutCandidates(proposal);
        return new AllocationProposalResponse(
                base.id(),
                base.paymentId(),
                base.status(),
                base.matchingMethod(),
                base.reason(),
                base.validatedBy(),
                base.validatedAt(),
                base.selectedDebtId(),
                candidates,
                base.version(),
                base.createdAt(),
                base.updatedAt());
    }

    @Mapping(target = "id", expression = "java(candidate.id())")
    @Mapping(target = "debtorId", expression = "java(candidate.debtorId())")
    @Mapping(target = "debtId", expression = "java(candidate.debtId())")
    @Mapping(target = "confidence", expression = "java(candidate.confidence())")
    @Mapping(target = "suggestedAmount", expression = "java(candidate.suggestedAmount())")
    @Mapping(target = "rankOrder", expression = "java(candidate.rankOrder())")
    @Mapping(target = "debt", ignore = true)
    @Mapping(target = "debtor", ignore = true)
    AllocationProposalCandidateResponse toAllocationProposalCandidateResponse(AllocationProposalCandidate candidate);

    @Mapping(target = "id", expression = "java(debt.id())")
    @Mapping(target = "debtorId", expression = "java(debt.debtorId())")
    @Mapping(target = "reference", expression = "java(debt.reference())")
    @Mapping(target = "originalAmount", expression = "java(debt.originalAmount())")
    @Mapping(target = "remainingAmount", expression = "java(debt.remainingAmount())")
    @Mapping(target = "currency", expression = "java(debt.currency())")
    @Mapping(target = "status", expression = "java(debt.status())")
    @Mapping(target = "dueDate", expression = "java(unwrap(debt.dueDate()))")
    @Mapping(target = "structuredCommunication", expression = "java(resolveStructuredCommunication(debt))")
    @Mapping(target = "freeCommunication", expression = "java(resolveFreeCommunication(debt))")
    @Mapping(target = "version", expression = "java(debt.version())")
    @Mapping(target = "createdAt", expression = "java(debt.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(debt.updatedAt())")
    ProposalCandidateDebtResponse toProposalCandidateDebtResponse(Debt debt);

    @Mapping(target = "id", expression = "java(debtor.id())")
    @Mapping(target = "type", expression = "java(debtor.type())")
    @Mapping(target = "displayName", expression = "java(debtor.displayName())")
    @Mapping(target = "nationalNumber", expression = "java(maskSensitiveNationalNumber(unwrap(debtor.nationalNumber())))")
    @Mapping(target = "enterpriseNumber", expression = "java(unwrap(debtor.enterpriseNumber()))")
    @Mapping(target = "active", expression = "java(debtor.active())")
    @Mapping(target = "createdAt", expression = "java(debtor.createdAt())")
    ProposalCandidateDebtorResponse toProposalCandidateDebtorResponse(Debtor debtor);

    @Mapping(target = "id", expression = "java(proposal.id())")
    @Mapping(target = "status", expression = "java(proposal.status())")
    @Mapping(target = "reason", expression = "java(unwrap(proposal.reason()))")
    @Mapping(target = "validatedBy", expression = "java(unwrap(proposal.validatedBy()))")
    @Mapping(target = "validatedAt", expression = "java(unwrap(proposal.validatedAt()))")
    @Mapping(target = "selectedDebtId", expression = "java(unwrap(proposal.selectedDebtId()))")
    @Mapping(target = "updatedAt", expression = "java(proposal.updatedAt())")
    ProposalStateResponse toProposalStateResponse(AllocationProposal proposal);

    @Mapping(target = "allocationId", expression = "java(allocation.id())")
    @Mapping(target = "proposalId", expression = "java(unwrap(allocation.proposalId()))")
    @Mapping(target = "paymentId", expression = "java(allocation.paymentId())")
    @Mapping(target = "debtId", expression = "java(allocation.debtId())")
    @Mapping(target = "amount", expression = "java(allocation.amount())")
    @Mapping(target = "status", expression = "java(allocation.status().name())")
    @Mapping(target = "createdBy", expression = "java(allocation.createdBy())")
    @Mapping(target = "createdAt", expression = "java(allocation.createdAt())")
    AllocationResultResponse toAllocationResultResponse(PaymentAllocation allocation);

    default AllocationProposalListResponse toAllocationProposalListResponse(UUID paymentId, List<AllocationProposal> proposals) {
        List<AllocationProposalSummaryResponse> responses = proposals.stream()
                .map(this::toAllocationProposalSummaryResponse)
                .toList();
        return new AllocationProposalListResponse(paymentId, responses);
    }

    default <T> T unwrap(Optional<T> value) {
        return value == null ? null : value.orElse(null);
    }

    default String resolveStructuredCommunication(Debt debt) {
        try {
            return StructuredCommunication.of(debt.reference()).formatted();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    default String resolveFreeCommunication(Debt debt) {
        return resolveStructuredCommunication(debt) == null ? debt.reference() : null;
    }

    @Named("maskSensitiveNationalNumber")
    default String maskSensitiveNationalNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() <= 6) {
            return "******";
        }
        return digits.substring(0, 6) + "*****";
    }
}
