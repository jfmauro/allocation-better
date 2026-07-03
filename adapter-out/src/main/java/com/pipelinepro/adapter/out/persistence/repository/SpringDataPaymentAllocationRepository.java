package com.pipelinepro.adapter.out.persistence.repository;

import com.pipelinepro.adapter.out.persistence.entity.PaymentAllocationEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPaymentAllocationRepository extends JpaRepository<PaymentAllocationEntity, UUID> {
    Optional<PaymentAllocationEntity> findByIdempotencyKey(String idempotencyKey);
}
