package com.pipelinepro.adapter.out.persistence.repository;

import com.pipelinepro.adapter.out.persistence.entity.IntakeRequestEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataIntakeRequestRepository extends JpaRepository<IntakeRequestEntity, UUID> {

    Optional<IntakeRequestEntity> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select intake from IntakeRequestEntity intake where intake.idempotencyKey = :idempotencyKey")
    Optional<IntakeRequestEntity> findByIdempotencyKeyForUpdate(@Param("idempotencyKey") String idempotencyKey);
}
