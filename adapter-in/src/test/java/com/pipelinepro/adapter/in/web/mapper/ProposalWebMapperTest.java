package com.pipelinepro.adapter.in.web.mapper;

import com.pipelinepro.adapter.in.web.v1.dto.request.ValidateProposalRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalCandidateResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalResponse;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.AllocationProposalCandidateDetails;
import com.pipelinepro.domain.AllocationProposalDetails;
import com.pipelinepro.domain.MatchConfidence;
import com.pipelinepro.domain.port.in.command.ValidateProposalCommand;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProposalWebMapperTest {

    private final ProposalWebMapper mapper = Mappers.getMapper(ProposalWebMapper.class);

    @Test
    void toValidateProposalCommand_shouldKeepActorAndReasonUnchanged() {
        UUID proposalId = UUID.randomUUID();
        ValidateProposalRequest request = new ValidateProposalRequest(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "john.doe",
                "Manual validation",
                Instant.parse("2026-07-03T12:00:00Z"));

        ValidateProposalCommand command = mapper.toValidateProposalCommand(
                proposalId,
                request,
                request.occurredAt());

        assertThat(command.actor()).isEqualTo("john.doe");
        assertThat(command.reason()).isEqualTo("Manual validation");
    }

    @Test
    void toAllocationProposalResponse_shouldMapCandidateDetails() {
        UUID proposalId = UUID.randomUUID();
        AllocationProposal proposal = AllocationProposal.proposed(
                proposalId,
                UUID.randomUUID(),
                com.pipelinepro.domain.MatchingMethod.IDENTIFIER,
                "candidate",
                Instant.parse("2026-07-03T12:00:00Z"));
        AllocationProposalCandidate candidate = new AllocationProposalCandidate(
                UUID.randomUUID(),
                proposalId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                MatchConfidence.HIGH,
                new BigDecimal("10.00"),
                0);
        AllocationProposalDetails details = new AllocationProposalDetails(
                proposal,
                List.of(new AllocationProposalCandidateDetails(candidate, null, null)));

        AllocationProposalResponse response = mapper.toAllocationProposalResponse(details);

        assertThat(response.id()).isEqualTo(proposalId);
        assertThat(response.candidates()).hasSize(1);
        AllocationProposalCandidateResponse candidateResponse = response.candidates().getFirst();
        assertThat(candidateResponse.id()).isEqualTo(candidate.id());
        assertThat(candidateResponse.debt()).isNull();
        assertThat(candidateResponse.debtor()).isNull();
    }
}
