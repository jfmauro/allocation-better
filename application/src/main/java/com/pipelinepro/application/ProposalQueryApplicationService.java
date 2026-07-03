package com.pipelinepro.application;

import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.port.in.GetProposalCandidatesUseCase;
import com.pipelinepro.domain.port.in.GetProposalDetailUseCase;
import com.pipelinepro.domain.port.out.AllocationProposalCandidateRepository;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ProposalQueryApplicationService implements GetProposalDetailUseCase, GetProposalCandidatesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProposalQueryApplicationService.class);

    private final AllocationProposalRepository allocationProposalRepository;
    private final AllocationProposalCandidateRepository allocationProposalCandidateRepository;

    public ProposalQueryApplicationService(
            AllocationProposalRepository allocationProposalRepository,
            AllocationProposalCandidateRepository allocationProposalCandidateRepository) {
        this.allocationProposalRepository = allocationProposalRepository;
        this.allocationProposalCandidateRepository = allocationProposalCandidateRepository;
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
    public List<AllocationProposalCandidate> listCandidates(UUID proposalId) {
        log.info("+++start listCandidates+++");
        try {
            return allocationProposalCandidateRepository.findByProposalId(proposalId);
        } finally {
            log.info("+++end listCandidates+++");
        }
    }
}
