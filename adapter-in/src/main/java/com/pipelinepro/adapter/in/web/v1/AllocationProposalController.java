package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.mapper.ProposalWebMapper;
import com.pipelinepro.adapter.in.web.error.NotFoundWebException;
import com.pipelinepro.adapter.in.web.v1.dto.request.MarkUnmatchedRequest;
import com.pipelinepro.adapter.in.web.v1.dto.request.RejectProposalRequest;
import com.pipelinepro.adapter.in.web.v1.dto.request.RequestInvestigationRequest;
import com.pipelinepro.adapter.in.web.v1.dto.request.SelectDebtRequest;
import com.pipelinepro.adapter.in.web.v1.dto.request.ValidateProposalRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalCandidateResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationResultResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.ProposalStateResponse;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationProposalDetails;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.in.GetAllocationProposalDetailsUseCase;
import com.pipelinepro.domain.port.in.ProposalLifecycleUseCase;
import com.pipelinepro.domain.port.in.command.MarkUnmatchedCommand;
import com.pipelinepro.domain.port.in.command.RejectProposalCommand;
import com.pipelinepro.domain.port.in.command.RequestInvestigationCommand;
import com.pipelinepro.domain.port.in.command.SelectDebtCommand;
import com.pipelinepro.domain.port.in.command.ValidateProposalCommand;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/allocation-proposals")
public class AllocationProposalController {

    private static final Logger log = LoggerFactory.getLogger(AllocationProposalController.class);

    private final ProposalLifecycleUseCase proposalLifecycleUseCase;
    private final GetAllocationProposalDetailsUseCase getAllocationProposalDetailsUseCase;
    private final ProposalWebMapper proposalWebMapper;

    public AllocationProposalController(
            ProposalLifecycleUseCase proposalLifecycleUseCase,
            GetAllocationProposalDetailsUseCase getAllocationProposalDetailsUseCase,
            ProposalWebMapper proposalWebMapper) {
        this.proposalLifecycleUseCase = proposalLifecycleUseCase;
        this.getAllocationProposalDetailsUseCase = getAllocationProposalDetailsUseCase;
        this.proposalWebMapper = proposalWebMapper;
    }

    @GetMapping("/{proposalId}")
    public ResponseEntity<AllocationProposalResponse> getProposal(@PathVariable UUID proposalId) {
        log.info("+++start getProposal+++");
        try {
            AllocationProposalDetails proposalDetails = getAllocationProposalDetailsUseCase.getProposalDetails(proposalId)
                    .orElseThrow(() -> new NotFoundWebException("Allocation proposal not found: " + proposalId));
            return ResponseEntity.ok(proposalWebMapper.toAllocationProposalResponse(proposalDetails));
        } finally {
            log.info("+++end getProposal+++");
        }
    }

    @PostMapping("/{proposalId}/validate")
    public ResponseEntity<AllocationResultResponse> validateProposal(
            @PathVariable UUID proposalId,
            @Valid @RequestBody ValidateProposalRequest request) {
        log.info("+++start validateProposal+++");
        try {
            Instant occurredAt = request.occurredAt() == null ? Instant.now() : request.occurredAt();
            ValidateProposalCommand command = proposalWebMapper.toValidateProposalCommand(proposalId, request, occurredAt);
            PaymentAllocation allocation = proposalLifecycleUseCase.validateProposal(command);
            return ResponseEntity.ok(proposalWebMapper.toAllocationResultResponse(allocation));
        } finally {
            log.info("+++end validateProposal+++");
        }
    }

    @PostMapping("/{proposalId}/reject")
    public ResponseEntity<ProposalStateResponse> rejectProposal(
            @PathVariable UUID proposalId,
            @Valid @RequestBody RejectProposalRequest request) {
        log.info("+++start rejectProposal+++");
        try {
            Instant occurredAt = request.occurredAt() == null ? Instant.now() : request.occurredAt();
            RejectProposalCommand command = proposalWebMapper.toRejectProposalCommand(proposalId, request, occurredAt);
            AllocationProposal proposal = proposalLifecycleUseCase.rejectProposal(command);
            return ResponseEntity.ok(proposalWebMapper.toProposalStateResponse(proposal));
        } finally {
            log.info("+++end rejectProposal+++");
        }
    }

    @PostMapping("/{proposalId}/select-debt")
    public ResponseEntity<ProposalStateResponse> selectDebt(
            @PathVariable UUID proposalId,
            @Valid @RequestBody SelectDebtRequest request) {
        log.info("+++start selectDebt+++");
        try {
            Instant occurredAt = request.occurredAt() == null ? Instant.now() : request.occurredAt();
            SelectDebtCommand command = proposalWebMapper.toSelectDebtCommand(proposalId, request, occurredAt);
            AllocationProposal proposal = proposalLifecycleUseCase.selectDebt(command);
            return ResponseEntity.ok(proposalWebMapper.toProposalStateResponse(proposal));
        } finally {
            log.info("+++end selectDebt+++");
        }
    }

    @PostMapping("/{proposalId}/mark-unmatched")
    public ResponseEntity<ProposalStateResponse> markUnmatched(
            @PathVariable UUID proposalId,
            @Valid @RequestBody MarkUnmatchedRequest request) {
        log.info("+++start markUnmatched+++");
        try {
            Instant occurredAt = request.occurredAt() == null ? Instant.now() : request.occurredAt();
            MarkUnmatchedCommand command = proposalWebMapper.toMarkUnmatchedCommand(proposalId, request, occurredAt);
            AllocationProposal proposal = proposalLifecycleUseCase.markUnmatched(command);
            return ResponseEntity.ok(proposalWebMapper.toProposalStateResponse(proposal));
        } finally {
            log.info("+++end markUnmatched+++");
        }
    }

    @PostMapping("/{proposalId}/request-investigation")
    public ResponseEntity<ProposalStateResponse> requestInvestigation(
            @PathVariable UUID proposalId,
            @Valid @RequestBody RequestInvestigationRequest request) {
        log.info("+++start requestInvestigation+++");
        try {
            Instant occurredAt = request.occurredAt() == null ? Instant.now() : request.occurredAt();
            RequestInvestigationCommand command = proposalWebMapper.toRequestInvestigationCommand(proposalId, request, occurredAt);
            AllocationProposal proposal = proposalLifecycleUseCase.requestInvestigation(command);
            return ResponseEntity.ok(proposalWebMapper.toProposalStateResponse(proposal));
        } finally {
            log.info("+++end requestInvestigation+++");
        }
    }

}
