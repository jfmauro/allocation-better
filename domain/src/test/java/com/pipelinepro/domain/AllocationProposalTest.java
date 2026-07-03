package com.pipelinepro.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocationProposalTest {

    @Test
    void should_validate_proposal_from_proposed_state() {
        Instant now = Instant.now();
        AllocationProposal proposal = AllocationProposal.proposed(
                UUID.randomUUID(),
                UUID.randomUUID(),
                MatchingMethod.NAME,
                "name match",
                now);

        proposal.validate("agent-a", now.plusSeconds(1));

        assertThat(proposal.status()).isEqualTo(ProposalStatus.VALIDATED);
        assertThat(proposal.validatedBy()).contains("agent-a");
        assertThat(proposal.validatedAt()).contains(now.plusSeconds(1));
    }

    @Test
    void should_allow_selecting_debt_while_proposed() {
        Instant now = Instant.now();
        AllocationProposal proposal = AllocationProposal.proposed(
                UUID.randomUUID(),
                UUID.randomUUID(),
                MatchingMethod.IDENTIFIER,
                null,
                now);
        UUID debtId = UUID.randomUUID();

        proposal.selectDebt("agent-b", debtId, now.plusSeconds(1));

        assertThat(proposal.selectedDebtId()).contains(debtId);
        assertThat(proposal.status()).isEqualTo(ProposalStatus.PROPOSED);
    }

    @Test
    void should_reject_state_change_when_not_proposed() {
        Instant now = Instant.now();
        AllocationProposal proposal = AllocationProposal.proposed(
                UUID.randomUUID(),
                UUID.randomUUID(),
                MatchingMethod.STRUCTURED_COMMUNICATION,
                null,
                now);
        proposal.validate("agent", now.plusSeconds(1));

        assertThatThrownBy(() -> proposal.reject("agent", "late rejection", now.plusSeconds(2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PROPOSED proposals can be changed");
    }
}
