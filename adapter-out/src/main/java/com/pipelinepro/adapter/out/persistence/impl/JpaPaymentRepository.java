package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.PaymentEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.out.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class JpaPaymentRepository implements PaymentRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaPaymentRepository.class);

    private final SpringDataPaymentRepository springDataPaymentRepository;
    private final PaymentEntityMapper paymentEntityMapper;

    public JpaPaymentRepository(
            SpringDataPaymentRepository springDataPaymentRepository,
            PaymentEntityMapper paymentEntityMapper) {
        this.springDataPaymentRepository = springDataPaymentRepository;
        this.paymentEntityMapper = paymentEntityMapper;
    }

    @Override
    public Payment save(Payment payment) {
        log.info("+++start save+++");
        try {
            var entity = paymentEntityMapper.toEntity(payment);
            if (payment.version() == 0L) {
                entity.setVersion(null);
                return paymentEntityMapper.toDomain(springDataPaymentRepository.saveAndFlush(entity));
            }
            entity.markNotNew();
            return paymentEntityMapper.toDomain(springDataPaymentRepository.saveAndFlush(entity));
        } finally {
            log.info("+++end save+++");
        }
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        log.info("+++start findById+++");
        try {
            return springDataPaymentRepository.findById(paymentId).map(paymentEntityMapper::toDomain);
        } finally {
            log.info("+++end findById+++");
        }
    }

    @Override
    public Optional<Payment> findByBankTransactionReference(String bankTransactionReference) {
        log.info("+++start findByBankTransactionReference+++");
        try {
            return springDataPaymentRepository.findByBankTransactionReference(bankTransactionReference)
                    .map(paymentEntityMapper::toDomain);
        } finally {
            log.info("+++end findByBankTransactionReference+++");
        }
    }
}
