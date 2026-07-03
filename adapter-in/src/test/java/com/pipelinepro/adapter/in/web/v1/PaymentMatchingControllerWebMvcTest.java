package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.error.GlobalRestExceptionHandler;
import com.pipelinepro.adapter.in.web.error.NotFoundWebException;
import com.pipelinepro.adapter.in.web.mapper.MatchingWebMapper;
import com.pipelinepro.adapter.in.web.v1.dto.response.MatchResultResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.ProposalCreationResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.StructuredMatchResponse;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.port.in.MatchPaymentUseCase;
import com.pipelinepro.domain.port.in.result.MatchPaymentResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentMatchingController.class)
@Import(GlobalRestExceptionHandler.class)
class PaymentMatchingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchPaymentUseCase matchPaymentUseCase;

    @MockitoBean
    private MatchingWebMapper matchingWebMapper;

    @Test
    void matchPayment_shouldReturn202_whenProposalIsCreated() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID proposalId = UUID.randomUUID();
        MatchPaymentResult result = MatchPaymentResult.proposalCreated(MatchingMethod.NAME, "Proposal created", proposalId);
        MatchResultResponse response = new MatchResultResponse(paymentId, MatchingMethod.NAME, "Proposal created", proposalId, false);

        when(matchPaymentUseCase.matchPayment(any())).thenReturn(result);
        when(matchingWebMapper.toMatchResultResponse(any(UUID.class), any())).thenReturn(response);

        mockMvc.perform(post("/payments/{paymentId}/match", paymentId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()));
    }

    @Test
    void matchStructuredCommunication_shouldReturn200_whenAutoAllocationExecuted() throws Exception {
        UUID paymentId = UUID.randomUUID();
        MatchPaymentResult result = MatchPaymentResult.autoAllocated(MatchingMethod.STRUCTURED_COMMUNICATION, "Auto allocated");
        StructuredMatchResponse response = new StructuredMatchResponse(paymentId, MatchingMethod.STRUCTURED_COMMUNICATION, "Auto allocated", null, true);

        when(matchPaymentUseCase.matchPayment(any())).thenReturn(result);
        when(matchingWebMapper.toStructuredMatchResponse(any(UUID.class), any())).thenReturn(response);

        mockMvc.perform(post("/payments/{paymentId}/match/structured-communication", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoAllocationExecuted").value(true));
    }

    @Test
    void matchIdentifier_shouldReturn202_whenProposalIsCreated() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID proposalId = UUID.randomUUID();
        MatchPaymentResult result = MatchPaymentResult.proposalCreated(MatchingMethod.IDENTIFIER, "Proposal created", proposalId);
        ProposalCreationResponse response = new ProposalCreationResponse(paymentId, MatchingMethod.IDENTIFIER, "Proposal created", proposalId);

        when(matchPaymentUseCase.matchPayment(any())).thenReturn(result);
        when(matchingWebMapper.toProposalCreationResponse(any(UUID.class), any())).thenReturn(response);

        mockMvc.perform(post("/payments/{paymentId}/match/identifier", paymentId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.proposalId").value(proposalId.toString()));
    }

    @Test
    void matchName_shouldReturn202_whenProposalIsCreated() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID proposalId = UUID.randomUUID();
        MatchPaymentResult result = MatchPaymentResult.proposalCreated(MatchingMethod.NAME, "Proposal created", proposalId);
        ProposalCreationResponse response = new ProposalCreationResponse(paymentId, MatchingMethod.NAME, "Proposal created", proposalId);

        when(matchPaymentUseCase.matchPayment(any())).thenReturn(result);
        when(matchingWebMapper.toProposalCreationResponse(any(UUID.class), any())).thenReturn(response);

        mockMvc.perform(post("/payments/{paymentId}/match/name", paymentId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.matchingMethod").value("NAME"));
    }

    @Test
    void matchIdentifier_shouldReturn400_whenIllegalStateIsThrown() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(matchPaymentUseCase.matchPayment(any())).thenThrow(new IllegalStateException("Invalid candidate payload"));

        mockMvc.perform(post("/payments/{paymentId}/match/identifier", paymentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void matchStructuredCommunication_shouldReturn409_whenIllegalStateIsThrown() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(matchPaymentUseCase.matchPayment(any())).thenThrow(new IllegalStateException("Invalid structured communication"));

        mockMvc.perform(post("/payments/{paymentId}/match/structured-communication", paymentId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void matchStructuredCommunication_shouldReturn400_whenIllegalArgumentIsThrown() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(matchPaymentUseCase.matchPayment(any())).thenThrow(new IllegalArgumentException("Invalid structured communication"));

        mockMvc.perform(post("/payments/{paymentId}/match/structured-communication", paymentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void matchName_shouldReturn404_whenPaymentDoesNotExist() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(matchPaymentUseCase.matchPayment(any())).thenThrow(new NotFoundWebException("Resource not found"));

        mockMvc.perform(post("/payments/{paymentId}/match/name", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void matchPayment_shouldReturn403ProblemDetail_whenSecurityExceptionIsThrown() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(matchPaymentUseCase.matchPayment(any())).thenThrow(new SecurityException("Forbidden operation"));

        mockMvc.perform(post("/payments/{paymentId}/match", paymentId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

}
