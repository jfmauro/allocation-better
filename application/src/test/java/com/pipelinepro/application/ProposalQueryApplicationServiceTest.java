package com.pipelinepro.application;

import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.AllocationProposalCandidateDetails;
import com.pipelinepro.domain.AllocationProposalDetails;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.MatchConfidence;
import com.pipelinepro.domain.port.out.AllocationProposalCandidateRepository;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import com.pipelinepro.domain.port.out.DebtorRepository;
import com.pipelinepro.domain.port.out.DebtRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProposalQueryApplicationServiceTest {

    @Test
    void should_load_all_candidate_context_in_two_batch_calls() {
        AllocationProposalRepository proposalRepository = mock(AllocationProposalRepository.class);
        AllocationProposalCandidateRepository candidateRepository = mock(AllocationProposalCandidateRepository.class);
        DebtRepository debtRepository = mock(DebtRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        QueryApplicationService queryApplicationService = new QueryApplicationService(
                mock(com.pipelinepro.domain.port.out.PaymentRepository.class),
                mock(AllocationProposalRepository.class),
                mock(com.pipelinepro.domain.port.out.PaymentAllocationRepository.class),
                debtRepository,
                debtorRepository);
        ProposalQueryApplicationService service = new ProposalQueryApplicationService(
                proposalRepository,
                candidateRepository,
                queryApplicationService,
                queryApplicationService);

        UUID proposalId = UUID.randomUUID();
        UUID debtorId = UUID.randomUUID();
        UUID debt1 = UUID.randomUUID();
        UUID debt2 = UUID.randomUUID();
        AllocationProposal proposal = AllocationProposal.proposed(
                proposalId,
                UUID.randomUUID(),
                com.pipelinepro.domain.MatchingMethod.IDENTIFIER,
                "candidate",
                Instant.parse("2026-07-01T10:00:00Z"));
        AllocationProposalCandidate candidate1 = new AllocationProposalCandidate(UUID.randomUUID(), proposalId, debtorId, debt1, MatchConfidence.HIGH, new BigDecimal("10.00"), 0);
        AllocationProposalCandidate candidate2 = new AllocationProposalCandidate(UUID.randomUUID(), proposalId, debtorId, debt2, MatchConfidence.MEDIUM, new BigDecimal("11.00"), 1);
        AllocationProposalCandidate candidate3 = new AllocationProposalCandidate(UUID.randomUUID(), proposalId, debtorId, debt2, MatchConfidence.LOW, new BigDecimal("12.00"), 2);
        Debt debtEntity1 = Debt.open(debt1, debtorId, "D-1", new BigDecimal("10.00"), "EUR", null, Instant.parse("2026-07-01T10:00:00Z"));
        Debt debtEntity2 = Debt.open(debt2, debtorId, "D-2", new BigDecimal("11.00"), "EUR", null, Instant.parse("2026-07-01T10:00:00Z"));
        Debtor debtor = Debtor.activeNaturalPerson(debtorId, "Batch Debtor", "85073003328", Instant.parse("2026-07-01T10:00:00Z"));

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(candidateRepository.findByProposalId(proposalId)).thenReturn(List.of(candidate1, candidate2, candidate3));
        when(debtRepository.findByIds(Set.of(debt1, debt2))).thenReturn(List.of(debtEntity1, debtEntity2));
        when(debtorRepository.findByIds(Set.of(debtorId))).thenReturn(List.of(debtor));

        Optional<AllocationProposalDetails> result = service.getProposalDetails(proposalId);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().candidates()).hasSize(3);
        verify(debtRepository).findByIds(Set.of(debt1, debt2));
        verify(debtorRepository).findByIds(Set.of(debtorId));
    }
}
