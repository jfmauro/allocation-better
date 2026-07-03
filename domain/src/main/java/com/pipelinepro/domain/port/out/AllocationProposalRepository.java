package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.AllocationProposal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AllocationProposalRepository {
    AllocationProposal save(AllocationProposal allocationProposal);

    Optional<AllocationProposal> findById(UUID proposalId);

    List<AllocationProposal> findByPaymentId(UUID paymentId);
}
