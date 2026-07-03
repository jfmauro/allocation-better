package com.pipelinepro.bootstrap.config;

import com.pipelinepro.application.AllocationExecutionApplicationService;
import com.pipelinepro.application.CreateDebtIntakeApplicationService;
import com.pipelinepro.application.CreateDebtorIntakeApplicationService;
import com.pipelinepro.application.PaymentIntakeApplicationService;
import com.pipelinepro.application.PaymentMatchingApplicationService;
import com.pipelinepro.application.ProposalLifecycleApplicationService;
import com.pipelinepro.application.ProposalQueryApplicationService;
import com.pipelinepro.application.QueryApplicationService;
import com.pipelinepro.application.port.out.DebtIntakeWorker;
import com.pipelinepro.application.port.out.DebtorIntakeWorker;
import com.pipelinepro.adapter.out.persistence.impl.JpaDebtIntakeTransactionalWorker;
import com.pipelinepro.adapter.out.persistence.impl.JpaDebtorIntakeTransactionalWorker;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.in.CreateDebtIntakeUseCase;
import com.pipelinepro.domain.port.in.CreateDebtorIntakeUseCase;
import com.pipelinepro.domain.port.in.ExecuteAllocationUseCase;
import com.pipelinepro.domain.port.in.GetAllocationDetailUseCase;
import com.pipelinepro.domain.port.in.GetProposalCandidatesUseCase;
import com.pipelinepro.domain.port.in.GetProposalDetailUseCase;
import com.pipelinepro.domain.port.in.QueryDebtorUseCase;
import com.pipelinepro.domain.port.in.MatchPaymentUseCase;
import com.pipelinepro.domain.port.in.ProposalLifecycleUseCase;
import com.pipelinepro.domain.port.in.QueryDebtUseCase;
import com.pipelinepro.domain.port.in.QueryPaymentUseCase;
import com.pipelinepro.domain.port.in.ReceivePaymentUseCase;
import com.pipelinepro.domain.port.in.command.CreateDebtCommand;
import com.pipelinepro.domain.port.in.command.CreateDebtorCommand;
import com.pipelinepro.domain.port.out.AllocationProposalCandidateRepository;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import com.pipelinepro.domain.port.out.AllocationTransactionalWorker;
import com.pipelinepro.domain.port.out.AuditEventGateway;
import com.pipelinepro.domain.port.out.DebtRepository;
import com.pipelinepro.domain.port.out.DebtorRepository;
import com.pipelinepro.domain.port.out.PaymentAllocationRepository;
import com.pipelinepro.domain.port.out.PaymentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationServiceConfig {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceConfig.class);

    @Bean
    public MatchPaymentUseCase matchPaymentUseCase(
            PaymentRepository paymentRepository,
            DebtorRepository debtorRepository,
            DebtRepository debtRepository,
            AllocationProposalRepository allocationProposalRepository,
            AllocationProposalCandidateRepository allocationProposalCandidateRepository,
            AuditEventGateway auditEventGateway,
            AllocationTransactionalWorker allocationTransactionalWorker) {
        log.info("+++start matchPaymentUseCase+++");
        try {
            return new PaymentMatchingApplicationService(
                    paymentRepository,
                    debtorRepository,
                    debtRepository,
                    allocationProposalRepository,
                    allocationProposalCandidateRepository,
                    auditEventGateway,
                    allocationTransactionalWorker);
        } finally {
            log.info("+++end matchPaymentUseCase+++");
        }
    }

    @Bean
    public ReceivePaymentUseCase receivePaymentUseCase(
            PaymentRepository paymentRepository,
            AuditEventGateway auditEventGateway,
            MatchPaymentUseCase matchPaymentUseCase) {
        log.info("+++start receivePaymentUseCase+++");
        try {
            return new PaymentIntakeApplicationService(paymentRepository, auditEventGateway, matchPaymentUseCase);
        } finally {
            log.info("+++end receivePaymentUseCase+++");
        }
    }

    @Bean
    public ProposalLifecycleUseCase proposalLifecycleUseCase(
            AllocationProposalRepository allocationProposalRepository,
            PaymentRepository paymentRepository,
            AllocationTransactionalWorker allocationTransactionalWorker,
            AuditEventGateway auditEventGateway) {
        log.info("+++start proposalLifecycleUseCase+++");
        try {
            return new ProposalLifecycleApplicationService(
                    allocationProposalRepository,
                    paymentRepository,
                    allocationTransactionalWorker,
                    auditEventGateway);
        } finally {
            log.info("+++end proposalLifecycleUseCase+++");
        }
    }

    @Bean
    public ExecuteAllocationUseCase executeAllocationUseCase(
            PaymentRepository paymentRepository,
            DebtRepository debtRepository,
            AllocationProposalRepository allocationProposalRepository,
            AllocationTransactionalWorker allocationTransactionalWorker) {
        log.info("+++start executeAllocationUseCase+++");
        try {
            return new AllocationExecutionApplicationService(
                    paymentRepository,
                    debtRepository,
                    allocationProposalRepository,
                    allocationTransactionalWorker);
        } finally {
            log.info("+++end executeAllocationUseCase+++");
        }
    }

    @Bean
    public DebtorIntakeWorker debtorIntakeWorker(JpaDebtorIntakeTransactionalWorker debtorIntakeTransactionalWorker) {
        log.info("+++start debtorIntakeWorker+++");
        try {
            return new DebtorIntakeWorkerAdapter(debtorIntakeTransactionalWorker);
        } finally {
            log.info("+++end debtorIntakeWorker+++");
        }
    }

    @Bean
    public DebtIntakeWorker debtIntakeWorker(JpaDebtIntakeTransactionalWorker debtIntakeTransactionalWorker) {
        log.info("+++start debtIntakeWorker+++");
        try {
            return new DebtIntakeWorkerAdapter(debtIntakeTransactionalWorker);
        } finally {
            log.info("+++end debtIntakeWorker+++");
        }
    }

    @Bean
    public CreateDebtorIntakeUseCase createDebtorIntakeUseCase(
            DebtorIntakeWorker debtorIntakeWorker,
            com.pipelinepro.domain.port.out.IntakeAuditEventGateway intakeAuditEventGateway) {
        log.info("+++start createDebtorIntakeUseCase+++");
        try {
            return new CreateDebtorIntakeApplicationService(debtorIntakeWorker, intakeAuditEventGateway);
        } finally {
            log.info("+++end createDebtorIntakeUseCase+++");
        }
    }

    @Bean
    public CreateDebtIntakeUseCase createDebtIntakeUseCase(
            DebtIntakeWorker debtIntakeWorker,
            com.pipelinepro.domain.port.out.IntakeAuditEventGateway intakeAuditEventGateway) {
        log.info("+++start createDebtIntakeUseCase+++");
        try {
            return new CreateDebtIntakeApplicationService(debtIntakeWorker, intakeAuditEventGateway);
        } finally {
            log.info("+++end createDebtIntakeUseCase+++");
        }
    }

    @Bean
    public QueryUseCases queryUseCases(
            PaymentRepository paymentRepository,
            AllocationProposalRepository allocationProposalRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            DebtRepository debtRepository,
            DebtorRepository debtorRepository) {
        log.info("+++start queryUseCases+++");
        try {
            QueryApplicationService delegate = new QueryApplicationService(
                    paymentRepository,
                    allocationProposalRepository,
                    paymentAllocationRepository,
                    debtRepository,
                    debtorRepository);
            return new QueryUseCases(delegate);
        } finally {
            log.info("+++end queryUseCases+++");
        }
    }

    @Bean
    public ProposalQueryUseCases proposalQueryUseCases(
            AllocationProposalRepository allocationProposalRepository,
            AllocationProposalCandidateRepository allocationProposalCandidateRepository) {
        log.info("+++start proposalQueryUseCases+++");
        try {
            ProposalQueryApplicationService delegate = new ProposalQueryApplicationService(
                    allocationProposalRepository,
                    allocationProposalCandidateRepository);
            return new ProposalQueryUseCases(delegate);
        } finally {
            log.info("+++end proposalQueryUseCases+++");
        }
    }

    private static final class QueryUseCases
            implements QueryPaymentUseCase, GetAllocationDetailUseCase, QueryDebtUseCase, QueryDebtorUseCase {

        private static final Logger log = LoggerFactory.getLogger(QueryUseCases.class);

        private final QueryApplicationService delegate;

        private QueryUseCases(QueryApplicationService delegate) {
            this.delegate = delegate;
        }

        @Override
        public Optional<Payment> getPayment(UUID paymentId) {
            log.info("+++start getPayment+++");
            try {
                return delegate.getPayment(paymentId);
            } finally {
                log.info("+++end getPayment+++");
            }
        }

        @Override
        public List<AllocationProposal> listProposals(UUID paymentId) {
            log.info("+++start listProposals+++");
            try {
                return delegate.listProposals(paymentId);
            } finally {
                log.info("+++end listProposals+++");
            }
        }

        @Override
        public Optional<PaymentAllocation> getAllocation(UUID allocationId) {
            log.info("+++start getAllocation+++");
            try {
                return delegate.getAllocation(allocationId);
            } finally {
                log.info("+++end getAllocation+++");
            }
        }

        @Override
        public Optional<Debt> getDebt(UUID debtId) {
            log.info("+++start getDebt+++");
            try {
                return delegate.getDebt(debtId);
            } finally {
                log.info("+++end getDebt+++");
            }
        }

        @Override
        public List<Debt> listDebtsByDebtor(UUID debtorId, List<DebtStatus> statuses) {
            log.info("+++start listDebtsByDebtor+++");
            try {
                return delegate.listDebtsByDebtor(debtorId, statuses);
            } finally {
                log.info("+++end listDebtsByDebtor+++");
            }
        }

        @Override
        public List<Debtor> listDebtors(com.pipelinepro.domain.port.in.command.DebtorSearchCriteria criteria) {
            log.info("+++start listDebtors+++");
            try {
                return delegate.listDebtors(criteria);
            } finally {
                log.info("+++end listDebtors+++");
            }
        }
    }

    private static final class ProposalQueryUseCases
            implements GetProposalDetailUseCase, GetProposalCandidatesUseCase {

        private static final Logger log = LoggerFactory.getLogger(ProposalQueryUseCases.class);

        private final ProposalQueryApplicationService delegate;

        private ProposalQueryUseCases(ProposalQueryApplicationService delegate) {
            this.delegate = delegate;
        }

        @Override
        public Optional<AllocationProposal> getProposal(UUID proposalId) {
            log.info("+++start getProposal+++");
            try {
                return delegate.getProposal(proposalId);
            } finally {
                log.info("+++end getProposal+++");
            }
        }

        @Override
        public List<AllocationProposalCandidate> listCandidates(UUID proposalId) {
            log.info("+++start listCandidates+++");
            try {
                return delegate.listCandidates(proposalId);
            } finally {
                log.info("+++end listCandidates+++");
            }
        }
    }

    private static final class DebtorIntakeWorkerAdapter implements DebtorIntakeWorker {

        private static final Logger log = LoggerFactory.getLogger(DebtorIntakeWorkerAdapter.class);

        private final JpaDebtorIntakeTransactionalWorker debtorIntakeTransactionalWorker;

        private DebtorIntakeWorkerAdapter(JpaDebtorIntakeTransactionalWorker debtorIntakeTransactionalWorker) {
            this.debtorIntakeTransactionalWorker = debtorIntakeTransactionalWorker;
        }

        @Override
        public Debtor createDebtor(CreateDebtorCommand command) {
            log.info("+++start createDebtor+++");
            try {
                return debtorIntakeTransactionalWorker.createDebtor(
                        command.debtorType(),
                        command.displayName(),
                        command.nationalNumber(),
                        command.enterpriseNumber(),
                        command.idempotencyKey(),
                        command.correlationId());
            } finally {
                log.info("+++end createDebtor+++");
            }
        }
    }

    private static final class DebtIntakeWorkerAdapter implements DebtIntakeWorker {

        private static final Logger log = LoggerFactory.getLogger(DebtIntakeWorkerAdapter.class);

        private final JpaDebtIntakeTransactionalWorker debtIntakeTransactionalWorker;

        private DebtIntakeWorkerAdapter(JpaDebtIntakeTransactionalWorker debtIntakeTransactionalWorker) {
            this.debtIntakeTransactionalWorker = debtIntakeTransactionalWorker;
        }

        @Override
        public Debt createDebt(CreateDebtCommand command) {
            log.info("+++start createDebt+++");
            try {
                return debtIntakeTransactionalWorker.createDebt(
                        command.debtorId(),
                        command.reference(),
                        command.originalAmount(),
                        command.currency(),
                        command.openingStatus(),
                        command.dueDate(),
                        command.idempotencyKey(),
                        command.correlationId());
            } finally {
                log.info("+++end createDebt+++");
            }
        }
    }
}
