package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueryPaymentUseCase {
    Optional<Payment> getPayment(UUID paymentId);

    List<AllocationProposal> listProposals(UUID paymentId);
}
