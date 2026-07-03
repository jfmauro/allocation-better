package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.mapper.PaymentWebMapper;
import com.pipelinepro.adapter.in.web.mapper.ProposalWebMapper;
import com.pipelinepro.adapter.in.web.error.NotFoundWebException;
import com.pipelinepro.adapter.in.web.v1.dto.request.ReceivePaymentRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalListResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.PaymentDetailsResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.PaymentResponse;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.in.QueryPaymentUseCase;
import com.pipelinepro.domain.port.in.ReceivePaymentUseCase;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final ReceivePaymentUseCase receivePaymentUseCase;
    private final QueryPaymentUseCase queryPaymentUseCase;
    private final PaymentWebMapper paymentWebMapper;
    private final ProposalWebMapper proposalWebMapper;

    public PaymentController(
            ReceivePaymentUseCase receivePaymentUseCase,
            QueryPaymentUseCase queryPaymentUseCase,
            PaymentWebMapper paymentWebMapper,
            ProposalWebMapper proposalWebMapper) {
        this.receivePaymentUseCase = receivePaymentUseCase;
        this.queryPaymentUseCase = queryPaymentUseCase;
        this.paymentWebMapper = paymentWebMapper;
        this.proposalWebMapper = proposalWebMapper;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody ReceivePaymentRequest request) {
        log.info("+++start createPayment+++");
        try {
            UUID paymentId = request.paymentId() == null ? UUID.randomUUID() : request.paymentId();
            ReceivePaymentCommand command = paymentWebMapper.toReceivePaymentCommand(request, paymentId);
            Payment payment = receivePaymentUseCase.receivePayment(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentWebMapper.toPaymentResponse(payment));
        } finally {
            log.info("+++end createPayment+++");
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailsResponse> getPayment(@PathVariable UUID paymentId) {
        log.info("+++start getPayment+++");
        try {
            Payment payment = queryPaymentUseCase.getPayment(paymentId)
                    .orElseThrow(() -> new NotFoundWebException("Payment not found: " + paymentId));
            return ResponseEntity.ok(paymentWebMapper.toPaymentDetailsResponse(payment));
        } finally {
            log.info("+++end getPayment+++");
        }
    }

    @GetMapping("/{paymentId}/proposals")
    public ResponseEntity<AllocationProposalListResponse> listPaymentProposals(@PathVariable UUID paymentId) {
        log.info("+++start listPaymentProposals+++");
        try {
            List<AllocationProposal> proposals = queryPaymentUseCase.listProposals(paymentId);
            if (proposals.isEmpty()) {
                throw new NotFoundWebException("Allocation proposals not found for payment: " + paymentId);
            }
            return ResponseEntity.ok(proposalWebMapper.toAllocationProposalListResponse(paymentId, proposals));
        } finally {
            log.info("+++end listPaymentProposals+++");
        }
    }

}
