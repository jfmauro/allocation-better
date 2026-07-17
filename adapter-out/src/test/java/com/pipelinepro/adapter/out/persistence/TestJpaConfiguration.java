package com.pipelinepro.adapter.out.persistence;

import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalCandidateRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAccountingEntryRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = {
        SpringDataPaymentRepository.class,
        SpringDataAllocationProposalCandidateRepository.class,
        SpringDataAccountingEntryRepository.class,
        SpringDataAuditEventRepository.class
})
class TestJpaConfiguration {
}
