package com.pipelinepro.application;

import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.in.GetAllocationDetailUseCase;
import com.pipelinepro.domain.port.in.GetProposalDetailUseCase;
import com.pipelinepro.domain.port.in.QueryDebtorUseCase;
import com.pipelinepro.domain.port.in.QueryDebtUseCase;
import com.pipelinepro.domain.port.in.QueryPaymentUseCase;
import com.pipelinepro.domain.port.in.command.DebtorSearchCriteria;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import com.pipelinepro.domain.port.out.DebtRepository;
import com.pipelinepro.domain.port.out.PaymentAllocationRepository;
import com.pipelinepro.domain.port.out.PaymentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryApplicationService implements
        QueryPaymentUseCase,
        GetProposalDetailUseCase,
        GetAllocationDetailUseCase,
        QueryDebtUseCase,
        QueryDebtorUseCase {

    private static final Logger log = LoggerFactory.getLogger(QueryApplicationService.class);

    private final PaymentRepository paymentRepository;
    private final AllocationProposalRepository allocationProposalRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final DebtRepository debtRepository;
    private final com.pipelinepro.domain.port.out.DebtorRepository debtorRepository;

    public QueryApplicationService(
            PaymentRepository paymentRepository,
            AllocationProposalRepository allocationProposalRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            DebtRepository debtRepository,
            com.pipelinepro.domain.port.out.DebtorRepository debtorRepository) {
        this.paymentRepository = paymentRepository;
        this.allocationProposalRepository = allocationProposalRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.debtRepository = debtRepository;
        this.debtorRepository = debtorRepository;
    }

    @Override
    public Optional<Payment> getPayment(UUID paymentId) {
        log.info("+++start getPayment+++");
        try {
            return paymentRepository.findById(paymentId);
        } finally {
            log.info("+++end getPayment+++");
        }
    }

    @Override
    public List<AllocationProposal> listProposals(UUID paymentId) {
        log.info("+++start listProposals+++");
        try {
            return allocationProposalRepository.findByPaymentId(paymentId);
        } finally {
            log.info("+++end listProposals+++");
        }
    }

    @Override
    public Optional<AllocationProposal> getProposal(UUID proposalId) {
        log.info("+++start getProposal+++");
        try {
            return allocationProposalRepository.findById(proposalId);
        } finally {
            log.info("+++end getProposal+++");
        }
    }

    @Override
    public Optional<PaymentAllocation> getAllocation(UUID allocationId) {
        log.info("+++start getAllocation+++");
        try {
            return paymentAllocationRepository.findById(allocationId);
        } finally {
            log.info("+++end getAllocation+++");
        }
    }

    @Override
    public Optional<Debt> getDebt(UUID debtId) {
        log.info("+++start getDebt+++");
        try {
            return debtRepository.findById(debtId);
        } finally {
            log.info("+++end getDebt+++");
        }
    }

    @Override
    public List<Debt> listDebtsByDebtor(UUID debtorId, List<DebtStatus> statuses) {
        log.info("+++start listDebtsByDebtor+++");
        try {
            List<Debt> debts = debtRepository.findByDebtorId(debtorId);
            List<DebtStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                    ? List.of(DebtStatus.OPEN, DebtStatus.PARTIALLY_PAID)
                    : statuses;
            return debts.stream()
                    .filter(debt -> effectiveStatuses.contains(debt.status()))
                    .toList();
        } finally {
            log.info("+++end listDebtsByDebtor+++");
        }
    }

    @Override
    public List<Debtor> listDebtors(DebtorSearchCriteria criteria) {
        log.info("+++start listDebtors+++");
        try {
            List<Debtor> debtors = Boolean.FALSE.equals(criteria == null ? null : criteria.active())
                    ? debtorRepository.findAll()
                    : debtorRepository.findAllActive();
            String query = criteria == null || criteria.query() == null ? "" : criteria.query().trim().toLowerCase();
            return debtors.stream()
                    .filter(debtor -> criteria == null || criteria.debtorType() == null || debtor.type() == criteria.debtorType())
                    .filter(debtor -> query.isEmpty() || debtor.displayName().toLowerCase().contains(query)
                            || debtor.enterpriseNumber().map(value -> value.toLowerCase().contains(query)).orElse(false)
                            || debtor.nationalNumber().map(value -> value.toLowerCase().contains(query)).orElse(false)
                            || debtor.id().toString().toLowerCase().contains(query))
                    .toList();
        } finally {
            log.info("+++end listDebtors+++");
        }
    }
}
