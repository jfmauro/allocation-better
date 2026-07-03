package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.AllocationProposalEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalRepository;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAllocationProposalRepository implements AllocationProposalRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaAllocationProposalRepository.class);

    private final SpringDataAllocationProposalRepository springDataAllocationProposalRepository;
    private final AllocationProposalEntityMapper allocationProposalEntityMapper;

    public JpaAllocationProposalRepository(
            SpringDataAllocationProposalRepository springDataAllocationProposalRepository,
            AllocationProposalEntityMapper allocationProposalEntityMapper) {
        this.springDataAllocationProposalRepository = springDataAllocationProposalRepository;
        this.allocationProposalEntityMapper = allocationProposalEntityMapper;
    }

    @Override
    public AllocationProposal save(AllocationProposal allocationProposal) {
        log.info("+++start save+++");
        try {
            var entity = allocationProposalEntityMapper.toEntity(allocationProposal);
            entity.markNotNew();
            try {
                return allocationProposalEntityMapper.toDomain(
                        springDataAllocationProposalRepository.saveAndFlush(entity));
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (allocationProposal.version() != 0L) {
                    throw ex;
                }
                entity.setId(null);
                entity.setVersion(null);
                return allocationProposalEntityMapper.toDomain(
                        springDataAllocationProposalRepository.saveAndFlush(entity));
            }
        } finally {
            log.info("+++end save+++");
        }
    }

    @Override
    public Optional<AllocationProposal> findById(UUID proposalId) {
        log.info("+++start findById+++");
        try {
            return springDataAllocationProposalRepository.findById(proposalId)
                    .map(allocationProposalEntityMapper::toDomain);
        } finally {
            log.info("+++end findById+++");
        }
    }

    @Override
    public List<AllocationProposal> findByPaymentId(UUID paymentId) {
        log.info("+++start findByPaymentId+++");
        try {
            return springDataAllocationProposalRepository.findByPaymentId(paymentId)
                    .stream()
                    .map(allocationProposalEntityMapper::toDomain)
                    .toList();
        } finally {
            log.info("+++end findByPaymentId+++");
        }
    }
}
