package com.pipelinepro.adapter.out.persistence.repository;

import com.pipelinepro.adapter.out.persistence.entity.AccountingEntryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataAccountingEntryRepository
        extends JpaRepository<AccountingEntryEntity, UUID>, JpaSpecificationExecutor<AccountingEntryEntity> {
}
