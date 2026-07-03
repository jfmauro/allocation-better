package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.PaymentAllocationEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentAllocationRepository;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.out.PaymentAllocationRepository;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class JpaPaymentAllocationRepository implements PaymentAllocationRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaPaymentAllocationRepository.class);

    private final SpringDataPaymentAllocationRepository springDataPaymentAllocationRepository;
    private final PaymentAllocationEntityMapper paymentAllocationEntityMapper;

    public JpaPaymentAllocationRepository(
            SpringDataPaymentAllocationRepository springDataPaymentAllocationRepository,
            PaymentAllocationEntityMapper paymentAllocationEntityMapper) {
        this.springDataPaymentAllocationRepository = springDataPaymentAllocationRepository;
        this.paymentAllocationEntityMapper = paymentAllocationEntityMapper;
    }

    @Override
    public PaymentAllocation save(PaymentAllocation paymentAllocation) {
        log.info("+++start save+++");
        try {
            return paymentAllocationEntityMapper.toDomain(
                    springDataPaymentAllocationRepository.saveAndFlush(
                            paymentAllocationEntityMapper.toEntity(paymentAllocation)));
        } finally {
            log.info("+++end save+++");
        }
    }

    @Override
    public Optional<PaymentAllocation> findById(UUID allocationId) {
        log.info("+++start findById+++");
        try {
            return springDataPaymentAllocationRepository.findById(allocationId)
                    .map(paymentAllocationEntityMapper::toDomain);
        } finally {
            log.info("+++end findById+++");
        }
    }

    @Override
    public Optional<PaymentAllocation> findByIdempotencyKey(String idempotencyKey) {
        log.info("+++start findByIdempotencyKey+++");
        try {
            return springDataPaymentAllocationRepository.findByIdempotencyKey(idempotencyKey)
                    .map(paymentAllocationEntityMapper::toDomain);
        } finally {
            log.info("+++end findByIdempotencyKey+++");
        }
    }
}
