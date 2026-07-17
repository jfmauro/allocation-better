package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.error.GlobalRestExceptionHandler;
import com.pipelinepro.adapter.in.web.mapper.PaymentWebMapper;
import com.pipelinepro.adapter.in.web.mapper.ProposalWebMapper;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalListResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalSummaryResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.PaymentDetailsResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.PaymentResponse;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentStatus;
import com.pipelinepro.domain.ProposalStatus;
import com.pipelinepro.domain.port.in.QueryPaymentUseCase;
import com.pipelinepro.domain.port.in.ReceivePaymentUseCase;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalRestExceptionHandler.class)
class PaymentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReceivePaymentUseCase receivePaymentUseCase;

    @MockitoBean
    private QueryPaymentUseCase queryPaymentUseCase;

    @MockitoBean
    private PaymentWebMapper paymentWebMapper;

    @MockitoBean
    private ProposalWebMapper proposalWebMapper;

    @Test
    void createPayment_shouldReturn201_whenRequestIsValid() throws Exception {
        ReceivePaymentCommand command = new ReceivePaymentCommand(
                UUID.randomUUID(),
                "TRX-001",
                Instant.parse("2026-06-01T10:15:30Z"),
                Instant.parse("2026-06-01T10:15:35Z"),
                new BigDecimal("100.00"),
                "EUR",
                "+++123/1234/12345+++",
                "raw payload",
                "raw payload",
                "John Doe",
                "BE68****",
                Instant.parse("2026-06-01T10:15:30Z"));
        PaymentResponse response = new PaymentResponse(
                UUID.randomUUID(),
                "TRX-001",
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "EUR",
                PaymentStatus.RECEIVED,
                Instant.parse("2026-06-01T10:15:30Z"),
                Instant.parse("2026-06-01T10:15:30Z"));

        when(paymentWebMapper.toReceivePaymentCommand(any(), any(UUID.class))).thenReturn(command);
        when(receivePaymentUseCase.receivePayment(any())).thenReturn(null);
        when(paymentWebMapper.toPaymentResponse(any())).thenReturn(response);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bankTransactionReference": "TRX-001",
                                  "executionDate": "2026-06-01T10:15:30Z",
                                  "valueDate": "2026-06-01T10:15:35Z",
                                  "amount": 100.00,
                                  "currency": "EUR",
                                  "payerName": "John Doe",
                                  "payerIban": "BE68539007547034",
                                  "structuredCommunication": "+++123/1234/12345+++",
                                  "rawBankMessage": "raw payload"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bankTransactionReference").value("TRX-001"));
    }

    @Test
    void createPayment_shouldReturn400ProblemDetail_whenRequestValidationFails() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 0,
                                  "currency": "USD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("bankTransactionReference")))
                .andExpect(jsonPath("$.path").value("/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPayment_shouldReturn409ProblemDetail_whenUseCaseThrowsIllegalState() throws Exception {
        when(paymentWebMapper.toReceivePaymentCommand(any(), any(UUID.class))).thenReturn(new ReceivePaymentCommand(
                UUID.randomUUID(),
                "TRX-001",
                null,
                Instant.parse("2026-06-01T10:15:00Z"),
                new BigDecimal("100.00"),
                "EUR",
                null,
                null,
                null,
                "John Doe",
                "BE****",
                Instant.parse("2026-06-01T10:15:30Z")));
        when(receivePaymentUseCase.receivePayment(any())).thenThrow(new IllegalStateException("Payment already exists"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bankTransactionReference": "TRX-001",
                                  "valueDate": "2026-06-01T10:15:00Z",
                                  "amount": 100.00,
                                  "currency": "EUR",
                                  "payerName": "John Doe"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflict detected"))
                .andExpect(jsonPath("$.path").value("/payments"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getPayment_shouldReturn200_whenPaymentExists() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentDetailsResponse response = new PaymentDetailsResponse(
                paymentId,
                "TRX-001",
                new BigDecimal("100.00"),
                new BigDecimal("25.00"),
                "EUR",
                Instant.parse("2026-06-01T10:15:30Z"),
                Instant.parse("2026-06-01T10:15:31Z"),
                PaymentStatus.PARTIALLY_ALLOCATED,
                "+++123/1234/12345+++",
                "info",
                "John Doe",
                "BE68****",
                1L,
                Instant.parse("2026-06-01T10:15:30Z"),
                Instant.parse("2026-06-02T10:15:30Z"));

        Payment payment = Payment.received(
                paymentId,
                "TRX-001",
                new BigDecimal("100.00"),
                "EUR",
                "+++123/1234/12345+++",
                "info",
                "John Doe",
                "BE68****",
                Instant.parse("2026-06-01T10:15:30Z"));

        when(queryPaymentUseCase.getPayment(paymentId)).thenReturn(Optional.of(payment));
        when(paymentWebMapper.toPaymentDetailsResponse(any())).thenReturn(response);

        mockMvc.perform(get("/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()));
    }

    @Test
    void getPayment_shouldReturn404_whenPaymentMissing() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(queryPaymentUseCase.getPayment(paymentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/payments/{paymentId}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void listPaymentProposals_shouldReturn200_whenProposalsExist() throws Exception {
        UUID paymentId = UUID.randomUUID();
        AllocationProposalListResponse response = new AllocationProposalListResponse(
                paymentId,
                List.of(new AllocationProposalSummaryResponse(
                        UUID.randomUUID(),
                        ProposalStatus.PROPOSED,
                        MatchingMethod.IDENTIFIER,
                        "candidate",
                        Instant.parse("2026-06-01T10:15:30Z"),
                        Instant.parse("2026-06-01T10:16:30Z"))));

        AllocationProposal proposal = AllocationProposal.proposed(
                UUID.randomUUID(),
                paymentId,
                MatchingMethod.IDENTIFIER,
                "candidate",
                Instant.parse("2026-06-01T10:15:30Z"));

        when(queryPaymentUseCase.listProposals(paymentId)).thenReturn(List.of(proposal));
        when(proposalWebMapper.toAllocationProposalListResponse(any(UUID.class), any())).thenReturn(response);

        mockMvc.perform(get("/payments/{paymentId}/proposals", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()));
    }

    @Test
    void listPaymentProposals_shouldReturn404_whenNoProposalExists() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(queryPaymentUseCase.listProposals(paymentId)).thenReturn(List.of());

        mockMvc.perform(get("/payments/{paymentId}/proposals", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }
}
