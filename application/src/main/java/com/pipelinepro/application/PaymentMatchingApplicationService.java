package com.pipelinepro.application;

import com.pipelinepro.application.error.ResourceNotFoundException;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalCandidate;
import com.pipelinepro.domain.AuditEvent;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.MatchConfidence;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentStatus;
import com.pipelinepro.domain.port.in.MatchPaymentUseCase;
import com.pipelinepro.domain.port.in.command.MatchPaymentCommand;
import com.pipelinepro.domain.port.in.result.MatchPaymentResult;
import com.pipelinepro.domain.port.out.AllocationProposalCandidateRepository;
import com.pipelinepro.domain.port.out.AllocationProposalRepository;
import com.pipelinepro.domain.port.out.AllocationTransactionalWorker;
import com.pipelinepro.domain.port.out.AuditEventGateway;
import com.pipelinepro.domain.port.out.DebtRepository;
import com.pipelinepro.domain.port.out.DebtorRepository;
import com.pipelinepro.domain.port.out.PaymentRepository;
import com.pipelinepro.domain.port.out.command.AllocationExecutionRequest;
import com.pipelinepro.domain.service.NameMatchScorer;
import com.pipelinepro.domain.value.EnterpriseNumber;
import com.pipelinepro.domain.value.NameNormalizer;
import com.pipelinepro.domain.value.NationalRegisterNumber;
import com.pipelinepro.domain.value.StructuredCommunication;
import com.pipelinepro.domain.value.VatNumber;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PaymentMatchingApplicationService implements MatchPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentMatchingApplicationService.class);
    private static final Pattern ENTERPRISE_LIKE_PATTERN = Pattern.compile("(?i)(?:BE)?\\s*[0-1]\\d[\\s./-]*\\d{3}[\\s./-]*\\d{3}[\\s./-]*\\d{2}");
    private static final Pattern NATIONAL_REGISTER_LIKE_PATTERN = Pattern.compile("(?<!\\d)(?:\\d{11}|\\d{2}[\\s./-]?\\d{2}[\\s./-]?\\d{2}[\\s./-]?\\d{3}[\\s./-]?\\d{2})(?!\\d)");

    private final PaymentRepository paymentRepository;
    private final DebtorRepository debtorRepository;
    private final DebtRepository debtRepository;
    private final AllocationProposalRepository allocationProposalRepository;
    private final AllocationProposalCandidateRepository allocationProposalCandidateRepository;
    private final AuditEventGateway auditEventGateway;
    private final AllocationTransactionalWorker allocationTransactionalWorker;
    private final NameMatchScorer nameMatchScorer;

    public PaymentMatchingApplicationService(
            PaymentRepository paymentRepository,
            DebtorRepository debtorRepository,
            DebtRepository debtRepository,
            AllocationProposalRepository allocationProposalRepository,
            AllocationProposalCandidateRepository allocationProposalCandidateRepository,
            AuditEventGateway auditEventGateway,
            AllocationTransactionalWorker allocationTransactionalWorker) {
        this(
                paymentRepository,
                debtorRepository,
                debtRepository,
                allocationProposalRepository,
                allocationProposalCandidateRepository,
                auditEventGateway,
                allocationTransactionalWorker,
                new NameMatchScorer(new NameNormalizer()));
    }

    PaymentMatchingApplicationService(
            PaymentRepository paymentRepository,
            DebtorRepository debtorRepository,
            DebtRepository debtRepository,
            AllocationProposalRepository allocationProposalRepository,
            AllocationProposalCandidateRepository allocationProposalCandidateRepository,
            AuditEventGateway auditEventGateway,
            AllocationTransactionalWorker allocationTransactionalWorker,
            NameMatchScorer nameMatchScorer) {
        this.paymentRepository = paymentRepository;
        this.debtorRepository = debtorRepository;
        this.debtRepository = debtRepository;
        this.allocationProposalRepository = allocationProposalRepository;
        this.allocationProposalCandidateRepository = allocationProposalCandidateRepository;
        this.auditEventGateway = auditEventGateway;
        this.allocationTransactionalWorker = allocationTransactionalWorker;
        this.nameMatchScorer = nameMatchScorer;
    }

    @Override
    public MatchPaymentResult matchPayment(MatchPaymentCommand command) {
        log.info("+++start matchPayment+++");
        try {
            Payment payment = paymentRepository.findById(command.paymentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

            appendMatchingAuditEvent(payment, "STRUCTURED_COMMUNICATION_NORMALIZED", null, command.requestedAt());
            StructuredMatchingAttemptResult structuredResult = attemptStructuredMatching(payment);
            appendMatchingAuditEvent(
                    payment,
                    structuredResult.hasAutoAllocationTarget()
                            ? "STRUCTURED_COMMUNICATION_VALIDATED"
                            : "STRUCTURED_COMMUNICATION_REJECTED",
                    structuredResult.reason(),
                    command.requestedAt());
            if (structuredResult.hasAutoAllocationTarget()) {
                executeStructuredAutoAllocation(
                        payment,
                        structuredResult.autoAllocationDebtOptional().orElseThrow(),
                        command.requestedAt());
                return MatchPaymentResult.autoAllocated(MatchingMethod.STRUCTURED_COMMUNICATION, structuredResult.reason());
            }

            if (command.matchingMethod() == MatchingMethod.STRUCTURED_COMMUNICATION) {
                AllocationProposal proposal = persistProposalWithCandidates(
                        payment,
                        MatchingMethod.STRUCTURED_COMMUNICATION,
                        MatchingAttemptResult.noCandidates(structuredResult.reason()),
                        command.requestedAt());
                return MatchPaymentResult.proposalCreated(
                        MatchingMethod.STRUCTURED_COMMUNICATION,
                        proposal.reason().orElse(""),
                        proposal.id());
            }

            MatchingAttemptResult identifierResult = attemptIdentifierMatching(payment);
            appendMatchingAuditEvent(payment, "IDENTIFIER_EXTRACTED", identifierResult.reason(), command.requestedAt());
            appendMatchingAuditEvent(
                    payment,
                    identifierResult.hasCandidates() ? "IDENTIFIER_VALIDATED" : "IDENTIFIER_REJECTED",
                    identifierResult.reason(),
                    command.requestedAt());
            if (identifierResult.hasCandidates()) {
                AllocationProposal proposal = persistProposalWithCandidates(payment, MatchingMethod.IDENTIFIER, identifierResult, command.requestedAt());
                return MatchPaymentResult.proposalCreated(MatchingMethod.IDENTIFIER, proposal.reason().orElse(""), proposal.id());
            }

            if (command.matchingMethod() == MatchingMethod.IDENTIFIER) {
                AllocationProposal proposal = persistProposalWithCandidates(payment, MatchingMethod.IDENTIFIER, identifierResult, command.requestedAt());
                return MatchPaymentResult.proposalCreated(MatchingMethod.IDENTIFIER, proposal.reason().orElse(""), proposal.id());
            }

            appendMatchingAuditEvent(payment, "NAME_MATCH_ATTEMPTED", null, command.requestedAt());
            MatchingAttemptResult nameResult = attemptNameMatching(payment);
            AllocationProposal proposal = persistProposalWithCandidates(payment, MatchingMethod.NAME, nameResult, command.requestedAt());
            return MatchPaymentResult.proposalCreated(MatchingMethod.NAME, proposal.reason().orElse(""), proposal.id());
        } finally {
            log.info("+++end matchPayment+++");
        }
    }

    private StructuredMatchingAttemptResult attemptStructuredMatching(Payment payment) {
        Optional<String> maybeStructured = payment.structuredCommunication();
        if (maybeStructured.isEmpty()) {
            return StructuredMatchingAttemptResult.noCandidates("STRUCTURED_COMMUNICATION_ABSENT");
        }

        StructuredCommunication structuredCommunication;
        try {
            structuredCommunication = StructuredCommunication.of(maybeStructured.orElseThrow());
        } catch (IllegalArgumentException invalidStructuredCommunication) {
            return StructuredMatchingAttemptResult.noCandidates("STRUCTURED_COMMUNICATION_INVALID");
        }

        List<Debt> candidateDebts = resolveStructuredCandidateDebts(payment, structuredCommunication);
        if (candidateDebts.isEmpty()) {
            return StructuredMatchingAttemptResult.noCandidates("STRUCTURED_COMMUNICATION_NO_ELIGIBLE_DEBT");
        }
        if (candidateDebts.size() > 1) {
            return StructuredMatchingAttemptResult.noCandidates("STRUCTURED_COMMUNICATION_AMBIGUOUS_DEBT");
        }
        return StructuredMatchingAttemptResult.withAutoAllocation(
                candidateDebts.getFirst(),
                "STRUCTURED_COMMUNICATION_UNIQUE_ELIGIBLE_DEBT");
    }

    private MatchingAttemptResult attemptIdentifierMatching(Payment payment) {
        Set<Debtor> matchedDebtors = resolveDebtorsFromIdentifiers(payment.freeCommunication().orElse(""));
        if (matchedDebtors.isEmpty()) {
            return MatchingAttemptResult.noCandidates("IDENTIFIER_NO_VALID_IDENTIFIER_FOUND");
        }

        List<CandidateSeed> candidates = buildCandidateSeeds(payment, matchedDebtors, debtor -> MatchConfidence.HIGH);
        if (candidates.isEmpty()) {
            return MatchingAttemptResult.noCandidates("IDENTIFIER_FOUND_BUT_NO_ELIGIBLE_DEBT");
        }
        return MatchingAttemptResult.withCandidates(candidates, "IDENTIFIER_MATCH_SUCCESS");
    }

    private MatchingAttemptResult attemptNameMatching(Payment payment) {
        List<String> nameInputs = resolveNameInputs(payment);
        if (nameInputs.isEmpty()) {
            return MatchingAttemptResult.noCandidates("NAME_MATCH_INPUT_ABSENT");
        }

        Set<Debtor> matchedDebtors = new HashSet<>(debtorRepository.findAllActive());
        if (matchedDebtors.isEmpty()) {
            return MatchingAttemptResult.noCandidates("NAME_MATCH_NO_ACTIVE_DEBTOR");
        }

        List<CandidateSeed> candidates = buildCandidateSeeds(payment, matchedDebtors, debtor -> scoreNameConfidence(nameInputs, debtor.displayName()));
        if (candidates.isEmpty()) {
            return MatchingAttemptResult.noCandidates("NAME_MATCH_NO_ELIGIBLE_CONFIDENT_CANDIDATE");
        }
        return MatchingAttemptResult.withCandidates(candidates, "NAME_MATCH_SUCCESS");
    }

    private List<String> resolveNameInputs(Payment payment) {
        List<String> values = new ArrayList<>();
        payment.payerName().map(String::trim).filter(value -> !value.isBlank()).ifPresent(values::add);
        payment.freeCommunication().map(String::trim).filter(value -> !value.isBlank()).ifPresent(values::add);
        return values;
    }

    private List<CandidateSeed> buildCandidateSeeds(
            Payment payment,
            Set<Debtor> debtors,
            java.util.function.Function<Debtor, MatchConfidence> confidenceProvider) {
        List<CandidateSeed> candidates = new ArrayList<>();
        Set<UUID> seenDebtIds = new HashSet<>();
        Map<UUID, MatchConfidence> confidenceByDebtId = new HashMap<>();
        Set<UUID> debtorIds = debtors.stream()
                .filter(Debtor::active)
                .map(Debtor::id)
                .collect(Collectors.toSet());
        Map<UUID, List<Debt>> debtsByDebtorId = debtRepository.findByDebtorIds(debtorIds).stream()
                .collect(Collectors.groupingBy(Debt::debtorId));
        for (Debtor debtor : debtors) {
            if (!debtor.active()) {
                continue;
            }

            MatchConfidence confidence = confidenceProvider.apply(debtor);
            if (confidence == MatchConfidence.LOW) {
                continue;
            }

            List<Debt> eligibleDebts = debtsByDebtorId.getOrDefault(debtor.id(), List.of()).stream()
                    .filter(this::isEligibleDebtStatus)
                    .filter(debt -> debt.currency().equals(payment.currency()))
                    .filter(debt -> debt.remainingAmount().compareTo(BigDecimal.ZERO) > 0)
                    .sorted(Comparator.comparing((Debt debt) -> debt.dueDate().orElse(null), Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            for (Debt debt : eligibleDebts) {
                if (seenDebtIds.add(debt.id())) {
                    confidenceByDebtId.put(debt.id(), confidence);
                    BigDecimal suggestedAmount = payment.remainingAmount().min(debt.remainingAmount());
                    candidates.add(new CandidateSeed(debtor.id(), debt.id(), confidence, suggestedAmount));
                    continue;
                }

                MatchConfidence previous = confidenceByDebtId.get(debt.id());
                if (previous != null && confidence.ordinal() > previous.ordinal()) {
                    confidenceByDebtId.put(debt.id(), confidence);
                    for (int i = 0; i < candidates.size(); i++) {
                        CandidateSeed existing = candidates.get(i);
                        if (existing.debtId().equals(debt.id())) {
                            candidates.set(i, new CandidateSeed(
                                    existing.debtorId(),
                                    existing.debtId(),
                                    confidence,
                                    existing.suggestedAmount()));
                            break;
                        }
                    }
                }
            }
        }
        return candidates;
    }

    private MatchConfidence scoreNameConfidence(List<String> candidateNames, String debtorDisplayName) {
        MatchConfidence best = MatchConfidence.LOW;
        for (String candidateName : candidateNames) {
            try {
                MatchConfidence current = nameMatchScorer.score(candidateName, debtorDisplayName);
                if (current == null) {
                    continue;
                }
                if (current.ordinal() > best.ordinal()) {
                    best = current;
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid candidate names and keep evaluating other available inputs.
            }
        }
        return best;
    }

    private Optional<Debtor> resolveDebtorByEnterpriseIdentifier(String rawIdentifier) {
        try {
            String enterpriseDigits = EnterpriseNumber.of(rawIdentifier).digits();
            return debtorRepository.findByEnterpriseNumber(enterpriseDigits);
        } catch (IllegalArgumentException ignored) {
            try {
                String vatDigits = VatNumber.belgian(rawIdentifier).digits();
                return debtorRepository.findByEnterpriseNumber(vatDigits);
            } catch (IllegalArgumentException ignoredVat) {
                return Optional.empty();
            }
        }
    }

    private Optional<Debtor> resolveDebtorByNationalRegisterIdentifier(String rawIdentifier) {
        try {
            String nationalDigits = NationalRegisterNumber.of(rawIdentifier).digits();
            return debtorRepository.findByNationalNumber(nationalDigits);
        } catch (IllegalArgumentException invalidNationalRegisterNumber) {
            return Optional.empty();
        }
    }

    private Set<Debtor> resolveDebtorsFromIdentifiers(String rawText) {
        Set<Debtor> debtors = new HashSet<>();
        for (String enterpriseIdentifier : extractEnterpriseLikeIdentifiers(rawText)) {
            resolveDebtorByEnterpriseIdentifier(enterpriseIdentifier).ifPresent(debtors::add);
        }
        for (String nationalIdentifier : extractNationalRegisterLikeIdentifiers(rawText)) {
            resolveDebtorByNationalRegisterIdentifier(nationalIdentifier).ifPresent(debtors::add);
        }
        return debtors;
    }

    private List<Debt> resolveStructuredCandidateDebts(Payment payment, StructuredCommunication structuredCommunication) {
        Map<UUID, Debt> uniqueDebts = new LinkedHashMap<>();
        for (String reference : Set.of(
                structuredCommunication.digits(),
                payment.structuredCommunication().orElse(structuredCommunication.digits()))) {
            for (Debt debt : debtRepository.findByReference(reference)) {
                if (isEligibleDebtStatus(debt)
                        && debt.currency().equals(payment.currency())
                        && debt.remainingAmount().compareTo(BigDecimal.ZERO) > 0) {
                    uniqueDebts.putIfAbsent(debt.id(), debt);
                }
            }
        }
        return List.copyOf(uniqueDebts.values());
    }

    private void executeStructuredAutoAllocation(Payment payment, Debt debt, Instant requestedAt) {
        BigDecimal amount = payment.remainingAmount().min(debt.remainingAmount());
        AllocationExecutionRequest request = new AllocationExecutionRequest(
                payment.id(),
                debt.id(),
                null,
                amount,
                structuredIdempotencyKey(payment.id(), debt.id()),
                structuredCommandId(payment.id(), debt.id()),
                "system-structured-matching",
                requestedAt);
        allocationTransactionalWorker.executeAllocation(request);
    }

    private void appendMatchingAuditEvent(Payment payment, String eventType, String reason, Instant occurredAt) {
        String payloadJson = "{\"paymentId\":\""
                + payment.id()
                + "\",\"eventType\":\""
                + escapeJson(eventType)
                + "\",\"reason\":\""
                + escapeJson(reason == null ? "" : reason)
                + "\"}";
        auditEventGateway.append(new AuditEvent(
                UUID.randomUUID(),
                "PAYMENT",
                payment.id(),
                eventType,
                null,
                payloadJson,
                occurredAt));
    }

    private String structuredIdempotencyKey(UUID paymentId, UUID debtId) {
        return "structured-auto:" + paymentId + ":" + debtId;
    }

    private String structuredCommandId(UUID paymentId, UUID debtId) {
        return "structured-auto-allocation:" + paymentId + ":" + debtId;
    }

    private Set<String> extractEnterpriseLikeIdentifiers(String rawText) {
        Set<String> identifiers = new HashSet<>();
        Matcher matcher = ENTERPRISE_LIKE_PATTERN.matcher(rawText == null ? "" : rawText);
        while (matcher.find()) {
            identifiers.add(matcher.group());
        }
        return identifiers;
    }

    private Set<String> extractNationalRegisterLikeIdentifiers(String rawText) {
        Set<String> identifiers = new HashSet<>();
        Matcher matcher = NATIONAL_REGISTER_LIKE_PATTERN.matcher(rawText == null ? "" : rawText);
        while (matcher.find()) {
            identifiers.add(matcher.group());
        }
        return identifiers;
    }

    private boolean isEligibleDebtStatus(Debt debt) {
        return debt.status() == DebtStatus.OPEN || debt.status() == DebtStatus.PARTIALLY_PAID;
    }

    private AllocationProposal persistProposalWithCandidates(
            Payment payment,
            MatchingMethod matchingMethod,
            MatchingAttemptResult result,
            Instant requestedAt) {
        AllocationProposal proposal = AllocationProposal.proposed(
                UUID.randomUUID(),
                payment.id(),
                matchingMethod,
                result.reason(),
                requestedAt);
        AllocationProposal persistedProposal = allocationProposalRepository.save(proposal);

        int rank = 0;
        for (CandidateSeed candidate : result.candidates()) {
            allocationProposalCandidateRepository.save(new AllocationProposalCandidate(
                    UUID.randomUUID(),
                    persistedProposal.id(),
                    candidate.debtorId(),
                    candidate.debtId(),
                    candidate.confidence(),
                    candidate.suggestedAmount(),
                    rank++));
        }

        if (result.hasCandidates()) {
            if (payment.status() == PaymentStatus.RECEIVED) {
                payment.markMatchProposed(requestedAt);
            }
            paymentRepository.save(payment);
            appendMatchProposedAuditEvent(payment, persistedProposal, result.reason(), requestedAt);
        } else {
            payment.markToMatch(requestedAt);
            paymentRepository.save(payment);
        }
        return persistedProposal;
    }

    private void appendMatchProposedAuditEvent(
            Payment payment,
            AllocationProposal proposal,
            String reason,
            Instant occurredAt) {
        String payloadJson = "{\"paymentId\":\""
                + payment.id()
                + "\",\"proposalId\":\""
                + proposal.id()
                + "\",\"reason\":\""
                + escapeJson(reason)
                + "\"}";
        auditEventGateway.append(new AuditEvent(
                UUID.randomUUID(),
                "PAYMENT",
                payment.id(),
                "MATCH_PROPOSED",
                null,
                payloadJson,
                occurredAt));
    }

    private String escapeJson(String value) {
        return (value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record CandidateSeed(
            UUID debtorId,
            UUID debtId,
            MatchConfidence confidence,
            BigDecimal suggestedAmount) {
    }

    private record MatchingAttemptResult(List<CandidateSeed> candidates, String reason) {
        static MatchingAttemptResult noCandidates(String reason) {
            return new MatchingAttemptResult(List.of(), reason);
        }

        static MatchingAttemptResult withCandidates(List<CandidateSeed> candidates, String reason) {
            return new MatchingAttemptResult(candidates, reason);
        }

        boolean hasCandidates() {
            return !candidates.isEmpty();
        }
    }

    private record StructuredMatchingAttemptResult(String reason, Debt autoAllocationDebt) {
        static StructuredMatchingAttemptResult noCandidates(String reason) {
            return new StructuredMatchingAttemptResult(reason, null);
        }

        static StructuredMatchingAttemptResult withAutoAllocation(Debt debt, String reason) {
            return new StructuredMatchingAttemptResult(reason, debt);
        }

        boolean hasAutoAllocationTarget() {
            return autoAllocationDebt != null;
        }

        Optional<Debt> autoAllocationDebtOptional() {
            return Optional.ofNullable(autoAllocationDebt);
        }
    }
}
