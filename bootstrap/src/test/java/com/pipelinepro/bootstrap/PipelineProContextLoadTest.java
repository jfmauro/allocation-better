package com.pipelinepro.bootstrap;

import com.pipelinepro.PipelineProApplication;
import com.pipelinepro.adapter.out.persistence.impl.JpaAllocationProposalCandidateRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaAllocationProposalRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaAllocationTransactionalWorker;
import com.pipelinepro.adapter.out.persistence.impl.JpaAuditEventGateway;
import com.pipelinepro.adapter.out.persistence.impl.JpaDebtIntakeTransactionalWorker;
import com.pipelinepro.adapter.out.persistence.impl.JpaDebtRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaDebtorIntakeTransactionalWorker;
import com.pipelinepro.adapter.out.persistence.impl.JpaDebtorRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaIntakeAuditEventGateway;
import com.pipelinepro.adapter.out.persistence.impl.JpaPaymentAllocationRepository;
import com.pipelinepro.adapter.out.persistence.impl.JpaPaymentRepository;
import com.pipelinepro.application.AllocationExecutionApplicationService;
import com.pipelinepro.application.CreateDebtIntakeApplicationService;
import com.pipelinepro.application.CreateDebtorIntakeApplicationService;
import com.pipelinepro.application.PaymentIntakeApplicationService;
import com.pipelinepro.application.PaymentMatchingApplicationService;
import com.pipelinepro.application.ProposalLifecycleApplicationService;
import com.pipelinepro.application.ProposalQueryApplicationService;
import com.pipelinepro.application.QueryApplicationService;
import com.pipelinepro.domain.port.in.CreateDebtIntakeUseCase;
import com.pipelinepro.domain.port.in.CreateDebtorIntakeUseCase;
import com.pipelinepro.domain.port.in.ExecuteAllocationUseCase;
import com.pipelinepro.domain.port.in.GetAllocationDetailUseCase;
import com.pipelinepro.domain.port.in.GetProposalCandidatesUseCase;
import com.pipelinepro.domain.port.in.GetProposalDetailUseCase;
import com.pipelinepro.domain.port.in.MatchPaymentUseCase;
import com.pipelinepro.domain.port.in.ProposalLifecycleUseCase;
import com.pipelinepro.domain.port.in.QueryDebtUseCase;
import com.pipelinepro.domain.port.in.QueryPaymentUseCase;
import com.pipelinepro.domain.port.in.ReceivePaymentUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PipelineProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PipelineProContextLoadTest {

    private final ApplicationContext applicationContext;

    private final Environment environment;

    PipelineProContextLoadTest(ApplicationContext applicationContext, Environment environment) {
        this.applicationContext = applicationContext;
        this.environment = environment;
    }

    @Test
    void should_loadContextWithExactlyOneInboundUseCaseBeanPerPort_when_bootstrapApplicationStarts() {
        assertThat(applicationContext.getBeansOfType(ReceivePaymentUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(MatchPaymentUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(CreateDebtorIntakeUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(CreateDebtIntakeUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(ProposalLifecycleUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(ExecuteAllocationUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(QueryPaymentUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(QueryDebtUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(GetAllocationDetailUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(GetProposalDetailUseCase.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(GetProposalCandidatesUseCase.class)).hasSize(1);
    }

    @Test
    void should_wireRealApplicationServicesWithAdapterOutPorts_when_bootstrapApplicationStarts() {
        ReceivePaymentUseCase receivePaymentUseCase = applicationContext.getBean(ReceivePaymentUseCase.class);
        assertThat(receivePaymentUseCase).isInstanceOf(PaymentIntakeApplicationService.class);
        assertThat(ReflectionTestUtils.getField(receivePaymentUseCase, "paymentRepository"))
                .isInstanceOf(JpaPaymentRepository.class);
        assertThat(ReflectionTestUtils.getField(receivePaymentUseCase, "auditEventGateway"))
                .isInstanceOf(JpaAuditEventGateway.class);
        assertThat(ReflectionTestUtils.getField(receivePaymentUseCase, "matchPaymentUseCase"))
                .isSameAs(applicationContext.getBean(MatchPaymentUseCase.class));

        MatchPaymentUseCase matchPaymentUseCase = applicationContext.getBean(MatchPaymentUseCase.class);
        assertThat(matchPaymentUseCase).isInstanceOf(PaymentMatchingApplicationService.class);
        assertThat(ReflectionTestUtils.getField(matchPaymentUseCase, "paymentRepository"))
                .isInstanceOf(JpaPaymentRepository.class);
        assertThat(ReflectionTestUtils.getField(matchPaymentUseCase, "debtorRepository"))
                .isInstanceOf(JpaDebtorRepository.class);
        assertThat(ReflectionTestUtils.getField(matchPaymentUseCase, "debtRepository"))
                .isInstanceOf(JpaDebtRepository.class);
        assertThat(ReflectionTestUtils.getField(matchPaymentUseCase, "allocationProposalRepository"))
                .isInstanceOf(JpaAllocationProposalRepository.class);
        assertThat(ReflectionTestUtils.getField(matchPaymentUseCase, "allocationProposalCandidateRepository"))
                .isInstanceOf(JpaAllocationProposalCandidateRepository.class);
        assertThat(ReflectionTestUtils.getField(matchPaymentUseCase, "auditEventGateway"))
                .isInstanceOf(JpaAuditEventGateway.class);
        assertThat(ReflectionTestUtils.getField(matchPaymentUseCase, "allocationTransactionalWorker"))
                .isInstanceOf(JpaAllocationTransactionalWorker.class);

        ProposalLifecycleUseCase proposalLifecycleUseCase = applicationContext.getBean(ProposalLifecycleUseCase.class);
        assertThat(proposalLifecycleUseCase).isInstanceOf(ProposalLifecycleApplicationService.class);
        assertThat(ReflectionTestUtils.getField(proposalLifecycleUseCase, "allocationProposalRepository"))
                .isInstanceOf(JpaAllocationProposalRepository.class);
        assertThat(ReflectionTestUtils.getField(proposalLifecycleUseCase, "paymentRepository"))
                .isInstanceOf(JpaPaymentRepository.class);
        assertThat(ReflectionTestUtils.getField(proposalLifecycleUseCase, "allocationTransactionalWorker"))
                .isInstanceOf(JpaAllocationTransactionalWorker.class);
        assertThat(ReflectionTestUtils.getField(proposalLifecycleUseCase, "auditEventGateway"))
                .isInstanceOf(JpaAuditEventGateway.class);

        ExecuteAllocationUseCase executeAllocationUseCase = applicationContext.getBean(ExecuteAllocationUseCase.class);
        assertThat(executeAllocationUseCase).isInstanceOf(AllocationExecutionApplicationService.class);
        assertThat(ReflectionTestUtils.getField(executeAllocationUseCase, "paymentRepository"))
                .isInstanceOf(JpaPaymentRepository.class);
        assertThat(ReflectionTestUtils.getField(executeAllocationUseCase, "debtRepository"))
                .isInstanceOf(JpaDebtRepository.class);
        assertThat(ReflectionTestUtils.getField(executeAllocationUseCase, "allocationProposalRepository"))
                .isInstanceOf(JpaAllocationProposalRepository.class);
        assertThat(ReflectionTestUtils.getField(executeAllocationUseCase, "allocationTransactionalWorker"))
                .isInstanceOf(JpaAllocationTransactionalWorker.class);

        CreateDebtorIntakeUseCase createDebtorIntakeUseCase = applicationContext.getBean(CreateDebtorIntakeUseCase.class);
        assertThat(createDebtorIntakeUseCase).isInstanceOf(CreateDebtorIntakeApplicationService.class);
        Object debtorIntakeWorker = ReflectionTestUtils.getField(createDebtorIntakeUseCase, "debtorIntakeWorker");
        assertThat(ReflectionTestUtils.getField(debtorIntakeWorker, "debtorIntakeTransactionalWorker"))
                .isInstanceOf(JpaDebtorIntakeTransactionalWorker.class);
        assertThat(ReflectionTestUtils.getField(createDebtorIntakeUseCase, "intakeAuditEventGateway"))
                .isInstanceOf(JpaIntakeAuditEventGateway.class);

        CreateDebtIntakeUseCase createDebtIntakeUseCase = applicationContext.getBean(CreateDebtIntakeUseCase.class);
        assertThat(createDebtIntakeUseCase).isInstanceOf(CreateDebtIntakeApplicationService.class);
        Object debtIntakeWorker = ReflectionTestUtils.getField(createDebtIntakeUseCase, "debtIntakeWorker");
        assertThat(ReflectionTestUtils.getField(debtIntakeWorker, "debtIntakeTransactionalWorker"))
                .isInstanceOf(JpaDebtIntakeTransactionalWorker.class);
        assertThat(ReflectionTestUtils.getField(createDebtIntakeUseCase, "intakeAuditEventGateway"))
                .isInstanceOf(JpaIntakeAuditEventGateway.class);

        QueryPaymentUseCase queryPaymentUseCase = applicationContext.getBean(QueryPaymentUseCase.class);
        assertThat(queryPaymentUseCase)
                .isSameAs(applicationContext.getBean(QueryDebtUseCase.class))
                .isSameAs(applicationContext.getBean(GetAllocationDetailUseCase.class));
        Object queryDelegate = ReflectionTestUtils.getField(queryPaymentUseCase, "delegate");
        assertThat(queryDelegate).isInstanceOf(QueryApplicationService.class);
        assertThat(ReflectionTestUtils.getField(queryDelegate, "paymentRepository"))
                .isInstanceOf(JpaPaymentRepository.class);
        assertThat(ReflectionTestUtils.getField(queryDelegate, "allocationProposalRepository"))
                .isInstanceOf(JpaAllocationProposalRepository.class);
        assertThat(ReflectionTestUtils.getField(queryDelegate, "paymentAllocationRepository"))
                .isInstanceOf(JpaPaymentAllocationRepository.class);
        assertThat(ReflectionTestUtils.getField(queryDelegate, "debtRepository"))
                .isInstanceOf(JpaDebtRepository.class);

        GetProposalDetailUseCase getProposalDetailUseCase = applicationContext.getBean(GetProposalDetailUseCase.class);
        assertThat(getProposalDetailUseCase)
                .isSameAs(applicationContext.getBean(GetProposalCandidatesUseCase.class));
        Object proposalQueryDelegate = ReflectionTestUtils.getField(getProposalDetailUseCase, "delegate");
        assertThat(proposalQueryDelegate).isInstanceOf(ProposalQueryApplicationService.class);
        assertThat(ReflectionTestUtils.getField(proposalQueryDelegate, "allocationProposalRepository"))
                .isInstanceOf(JpaAllocationProposalRepository.class);
        assertThat(ReflectionTestUtils.getField(proposalQueryDelegate, "allocationProposalCandidateRepository"))
                .isInstanceOf(JpaAllocationProposalCandidateRepository.class);
    }
}
