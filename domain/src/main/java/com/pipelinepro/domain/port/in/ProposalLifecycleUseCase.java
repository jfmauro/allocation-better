package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.in.command.MarkUnmatchedCommand;
import com.pipelinepro.domain.port.in.command.RejectProposalCommand;
import com.pipelinepro.domain.port.in.command.RequestInvestigationCommand;
import com.pipelinepro.domain.port.in.command.SelectDebtCommand;
import com.pipelinepro.domain.port.in.command.ValidateProposalCommand;

public interface ProposalLifecycleUseCase {
    PaymentAllocation validateProposal(ValidateProposalCommand command);

    AllocationProposal rejectProposal(RejectProposalCommand command);

    AllocationProposal selectDebt(SelectDebtCommand command);

    AllocationProposal markUnmatched(MarkUnmatchedCommand command);

    AllocationProposal requestInvestigation(RequestInvestigationCommand command);
}
