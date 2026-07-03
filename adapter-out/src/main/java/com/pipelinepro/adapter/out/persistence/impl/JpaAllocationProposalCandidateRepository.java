package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.AllocationProposalCandidateEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalCandidateRepository;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.port.out.AllocationProposalCandidateRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAllocationProposalCandidateRepository implements AllocationProposalCandidateRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaAllocationProposalCandidateRepository.class);

    private final SpringDataAllocationProposalCandidateRepository springDataAllocationProposalCandidateRepository;
    private final AllocationProposalCandidateEntityMapper allocationProposalCandidateEntityMapper;

    public JpaAllocationProposalCandidateRepository(
            SpringDataAllocationProposalCandidateRepository springDataAllocationProposalCandidateRepository,
            AllocationProposalCandidateEntityMapper allocationProposalCandidateEntityMapper) {
        this.springDataAllocationProposalCandidateRepository = springDataAllocationProposalCandidateRepository;
        this.allocationProposalCandidateEntityMapper = allocationProposalCandidateEntityMapper;
    }

    @Override
    public AllocationProposalCandidate save(AllocationProposalCandidate candidate) {
        log.info("+++start save+++");
        try {
            var entity = allocationProposalCandidateEntityMapper.toEntity(candidate);
            entity.markNotNew();
            try {
                return allocationProposalCandidateEntityMapper.toDomain(
                        springDataAllocationProposalCandidateRepository.saveAndFlush(entity));
            } catch (ObjectOptimisticLockingFailureException ex) {
                entity.setId(null);
                return allocationProposalCandidateEntityMapper.toDomain(
                        springDataAllocationProposalCandidateRepository.saveAndFlush(entity));
            }
        } finally {
            log.info("+++end save+++");
        }
    }

    @Override
    public Optional<AllocationProposalCandidate> findById(UUID candidateId) {
        log.info("+++start findById+++");
        try {
            return springDataAllocationProposalCandidateRepository.findById(candidateId)
                    .map(allocationProposalCandidateEntityMapper::toDomain);
        } finally {
            log.info("+++end findById+++");
        }
    }

    @Override
    public List<AllocationProposalCandidate> findByProposalId(UUID proposalId) {
        log.info("+++start findByProposalId+++");
        try {
            return springDataAllocationProposalCandidateRepository.findByProposalId(proposalId)
                    .stream()
                    .map(allocationProposalCandidateEntityMapper::toDomain)
                    .toList();
        } finally {
            log.info("+++end findByProposalId+++");
        }
    }
}
