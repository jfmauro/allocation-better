package com.pipelinepro.adapter.out.persistence.repository;

import com.pipelinepro.adapter.out.persistence.entity.NationalNumberAccessLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNationalNumberAccessLogRepository extends JpaRepository<NationalNumberAccessLogEntity, UUID> {
}
