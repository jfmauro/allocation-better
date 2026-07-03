package com.pipelinepro.adapter.out.persistence;

import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalCandidateRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = {
        SpringDataPaymentRepository.class,
        SpringDataAllocationProposalCandidateRepository.class
})
class TestJpaConfiguration {
}
