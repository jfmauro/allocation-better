package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.mapper.MatchingWebMapper;
import com.pipelinepro.adapter.in.web.error.BadRequestWebException;
import com.pipelinepro.adapter.in.web.error.ConflictWebException;
import com.pipelinepro.adapter.in.web.v1.dto.response.MatchResultResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.ProposalCreationResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.StructuredMatchResponse;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.port.in.MatchPaymentUseCase;
import com.pipelinepro.domain.port.in.command.MatchPaymentCommand;
import com.pipelinepro.domain.port.in.result.MatchPaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentMatchingController {

    private static final Logger log = LoggerFactory.getLogger(PaymentMatchingController.class);

    private final MatchPaymentUseCase matchPaymentUseCase;
    private final MatchingWebMapper matchingWebMapper;

    public PaymentMatchingController(
            MatchPaymentUseCase matchPaymentUseCase,
            MatchingWebMapper matchingWebMapper) {
        this.matchPaymentUseCase = matchPaymentUseCase;
        this.matchingWebMapper = matchingWebMapper;
    }

    @PostMapping("/{paymentId}/match")
    public ResponseEntity<MatchResultResponse> matchPayment(@PathVariable UUID paymentId) {
        log.info("+++start matchPayment+++");
        try {
            MatchPaymentResult result = executeMatchForConflictContract(paymentId, MatchingMethod.NAME);
            MatchResultResponse response = matchingWebMapper.toMatchResultResponse(paymentId, result);
            return ResponseEntity.status(resolveMatchStatus(result)).body(response);
        } finally {
            log.info("+++end matchPayment+++");
        }
    }

    @PostMapping("/{paymentId}/match/structured-communication")
    public ResponseEntity<StructuredMatchResponse> matchStructuredCommunication(@PathVariable UUID paymentId) {
        log.info("+++start matchStructuredCommunication+++");
        try {
            MatchPaymentResult result = executeMatchForStructuredCommunicationContract(paymentId);
            StructuredMatchResponse response = matchingWebMapper.toStructuredMatchResponse(paymentId, result);
            return ResponseEntity.status(resolveMatchStatus(result)).body(response);
        } finally {
            log.info("+++end matchStructuredCommunication+++");
        }
    }

    @PostMapping("/{paymentId}/match/identifier")
    public ResponseEntity<ProposalCreationResponse> matchIdentifier(@PathVariable UUID paymentId) {
        log.info("+++start matchIdentifier+++");
        try {
            MatchPaymentResult result = executeMatchForBadRequestContract(paymentId, MatchingMethod.IDENTIFIER);
            ProposalCreationResponse response = matchingWebMapper.toProposalCreationResponse(paymentId, result);
            return ResponseEntity.status(resolveMatchStatus(result)).body(response);
        } finally {
            log.info("+++end matchIdentifier+++");
        }
    }

    @PostMapping("/{paymentId}/match/name")
    public ResponseEntity<ProposalCreationResponse> matchByName(@PathVariable UUID paymentId) {
        log.info("+++start matchByName+++");
        try {
            MatchPaymentResult result = executeMatchForBadRequestContract(paymentId, MatchingMethod.NAME);
            ProposalCreationResponse response = matchingWebMapper.toProposalCreationResponse(paymentId, result);
            return ResponseEntity.status(resolveMatchStatus(result)).body(response);
        } finally {
            log.info("+++end matchByName+++");
        }
    }

    private MatchPaymentResult executeMatch(UUID paymentId, MatchingMethod matchingMethod) {
        return matchPaymentUseCase.matchPayment(new MatchPaymentCommand(paymentId, matchingMethod, Instant.now()));
    }

    private MatchPaymentResult executeMatchForBadRequestContract(UUID paymentId, MatchingMethod matchingMethod) {
        try {
            return executeMatch(paymentId, matchingMethod);
        } catch (IllegalStateException exception) {
            throw new BadRequestWebException("Bad request");
        }
    }

    private MatchPaymentResult executeMatchForStructuredCommunicationContract(UUID paymentId) {
        try {
            return executeMatch(paymentId, MatchingMethod.STRUCTURED_COMMUNICATION);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestWebException("Bad request");
        } catch (IllegalStateException exception) {
            throw new ConflictWebException("Conflict detected");
        }
    }

    private MatchPaymentResult executeMatchForConflictContract(UUID paymentId, MatchingMethod matchingMethod) {
        try {
            return executeMatch(paymentId, matchingMethod);
        } catch (IllegalStateException exception) {
            throw new ConflictWebException("Conflict detected");
        }
    }

    private HttpStatus resolveMatchStatus(MatchPaymentResult result) {
        if (result.autoAllocationExecuted()) {
            return HttpStatus.OK;
        }
        if (result.proposalId().isPresent()) {
            return HttpStatus.ACCEPTED;
        }
        return HttpStatus.OK;
    }
}
