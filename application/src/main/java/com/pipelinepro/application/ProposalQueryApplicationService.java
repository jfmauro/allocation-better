package com.pipelinepro.application;

import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.AllocationProposalCandidateDetails;
import com.pipelinepro.domain.AllocationProposalDetails;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.in.GetAllocationProposalDetailsUseCase;
import com.pipelinepro.domain.port.in.GetProposalCandidatesUseCase;
import com.pipelinepro.domain.port.in.GetProposalDetailUseCase;
import com.pipelinepro.domain.port.in.QueryDebtorUseCase;
import com.pipelinepro.domain.port.in.QueryDebtUseCase;
import com.pipelinepro.domain.port.out.AllocationProposalCandidateRepository;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ProposalQueryApplicationService implements GetAllocationProposalDetailsUseCase, GetProposalDetailUseCase, GetProposalCandidatesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProposalQueryApplicationService.class);

    private final AllocationProposalRepository allocationProposalRepository;
    private final AllocationProposalCandidateRepository allocationProposalCandidateRepository;
    private final QueryDebtUseCase queryDebtUseCase;
    private final QueryDebtorUseCase queryDebtorUseCase;

    public ProposalQueryApplicationService(
            AllocationProposalRepository allocationProposalRepository,
            AllocationProposalCandidateRepository allocationProposalCandidateRepository,
            QueryDebtUseCase queryDebtUseCase,
            QueryDebtorUseCase queryDebtorUseCase) {
        this.allocationProposalRepository = allocationProposalRepository;
        this.allocationProposalCandidateRepository = allocationProposalCandidateRepository;
        this.queryDebtUseCase = queryDebtUseCase;
        this.queryDebtorUseCase = queryDebtorUseCase;
    }

    @Override
    public Optional<AllocationProposal> getProposal(UUID proposalId) {
        log.info("+++start getProposal+++");
        try {
            return allocationProposalRepository.findById(proposalId);
        } finally {
            log.info("+++end getProposal+++");
        }
    }

    @Override
    public Optional<AllocationProposalDetails> getProposalDetails(UUID proposalId) {
        log.info("+++start getProposalDetails+++");
        try {
            Optional<AllocationProposal> proposal = allocationProposalRepository.findById(proposalId);
            if (proposal.isEmpty()) {
                return Optional.empty();
            }
            List<AllocationProposalCandidate> candidates = allocationProposalCandidateRepository.findByProposalId(proposalId);
            Set<UUID> debtIds = candidates.stream().map(AllocationProposalCandidate::debtId).collect(Collectors.toSet());
            Set<UUID> debtorIds = candidates.stream().map(AllocationProposalCandidate::debtorId).collect(Collectors.toSet());
            Map<UUID, Debt> debtsById = queryDebtUseCase.getDebts(debtIds).stream()
                    .collect(Collectors.toMap(Debt::id, Function.identity(), (left, right) -> left));
            Map<UUID, Debtor> debtorsById = queryDebtorUseCase.listDebtors(debtorIds).stream()
                    .collect(Collectors.toMap(Debtor::id, Function.identity(), (left, right) -> left));
            List<AllocationProposalCandidateDetails> candidateDetails = candidates.stream()
                    .map(candidate -> new AllocationProposalCandidateDetails(
                            candidate,
                            debtsById.get(candidate.debtId()),
                            debtorsById.get(candidate.debtorId())))
                    .toList();
            return Optional.of(new AllocationProposalDetails(proposal.get(), candidateDetails));
        } finally {
            log.info("+++end getProposalDetails+++");
        }
    }

    @Override
    public List<AllocationProposalCandidate> listCandidates(UUID proposalId) {
        log.info("+++start listCandidates+++");
        try {
            return allocationProposalCandidateRepository.findByProposalId(proposalId);
        } finally {
            log.info("+++end listCandidates+++");
        }
    }
}
