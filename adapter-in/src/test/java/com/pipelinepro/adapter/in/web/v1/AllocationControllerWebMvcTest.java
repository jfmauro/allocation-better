package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.error.GlobalRestExceptionHandler;
import com.pipelinepro.adapter.in.web.error.NotFoundWebException;
import com.pipelinepro.adapter.in.web.mapper.AllocationWebMapper;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationResultResponse;
import com.pipelinepro.domain.AllocationStatus;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.in.ExecuteAllocationUseCase;
import com.pipelinepro.domain.port.in.GetAllocationDetailUseCase;
import com.pipelinepro.domain.port.in.command.ExecuteAllocationCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AllocationController.class)
@Import(GlobalRestExceptionHandler.class)
class AllocationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecuteAllocationUseCase executeAllocationUseCase;

    @MockitoBean
    private GetAllocationDetailUseCase getAllocationDetailUseCase;

    @MockitoBean
    private AllocationWebMapper allocationWebMapper;

    @Test
    void createAllocation_shouldReturn201_whenRequestIsValid() throws Exception {
        ExecuteAllocationCommand command = new ExecuteAllocationCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new BigDecimal("50.00"),
                "idem-1",
                "cmd-1",
                "system",
                Instant.parse("2026-06-01T10:00:00Z"));
        AllocationResultResponse response = new AllocationResultResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                "ALLOCATED",
                "system",
                Instant.parse("2026-06-01T10:00:00Z"));

        when(allocationWebMapper.toExecuteAllocationCommand(any(), any(Instant.class))).thenReturn(command);
        when(executeAllocationUseCase.executeAllocation(any())).thenReturn(null);
        when(allocationWebMapper.toAllocationResultResponse(any())).thenReturn(response);

        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "8f8c9b2f-9089-4b76-979d-a8bdcebb0873",
                                  "debtId": "1b70ecc8-c24f-453f-9708-c46a27d9154f",
                                  "amount": 50.00,
                                  "idempotencyKey": "idem-1",
                                  "commandId": "cmd-1",
                                  "actor": "system"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ALLOCATED"));
    }

    @Test
    void createAllocation_shouldReturn400ProblemDetail_whenValidationFails() throws Exception {
        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 0,
                                  "actor": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("paymentId")));
    }

    @Test
    void createAllocation_shouldReturn400ProblemDetail_whenUseCaseThrowsIllegalArgument() throws Exception {
        ExecuteAllocationCommand command = new ExecuteAllocationCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new BigDecimal("50.00"),
                "idem-1",
                "cmd-1",
                "system",
                Instant.parse("2026-06-01T10:00:00Z"));
        when(allocationWebMapper.toExecuteAllocationCommand(any(), any(Instant.class))).thenReturn(command);
        when(executeAllocationUseCase.executeAllocation(any())).thenThrow(new IllegalArgumentException("Invalid amount"));

        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "8f8c9b2f-9089-4b76-979d-a8bdcebb0873",
                                  "debtId": "1b70ecc8-c24f-453f-9708-c46a27d9154f",
                                  "amount": 50.00,
                                  "idempotencyKey": "idem-1",
                                  "commandId": "cmd-1",
                                  "actor": "system"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad request"));
    }

    @Test
    void createAllocation_shouldReturn404_whenPaymentMissing() throws Exception {
        ExecuteAllocationCommand command = new ExecuteAllocationCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new BigDecimal("50.00"),
                "idem-1",
                "cmd-1",
                "system",
                Instant.parse("2026-06-01T10:00:00Z"));
        when(allocationWebMapper.toExecuteAllocationCommand(any(), any(Instant.class))).thenReturn(command);
        when(executeAllocationUseCase.executeAllocation(any())).thenThrow(new NotFoundWebException("Resource not found"));

        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "8f8c9b2f-9089-4b76-979d-a8bdcebb0873",
                                  "debtId": "1b70ecc8-c24f-453f-9708-c46a27d9154f",
                                  "amount": 50.00,
                                  "idempotencyKey": "idem-1",
                                  "commandId": "cmd-1",
                                  "actor": "system"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createAllocation_shouldReturn404_whenDebtMissing() throws Exception {
        ExecuteAllocationCommand command = new ExecuteAllocationCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new BigDecimal("50.00"),
                "idem-1",
                "cmd-1",
                "system",
                Instant.parse("2026-06-01T10:00:00Z"));
        when(allocationWebMapper.toExecuteAllocationCommand(any(), any(Instant.class))).thenReturn(command);
        when(executeAllocationUseCase.executeAllocation(any())).thenThrow(new NotFoundWebException("Resource not found"));

        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "8f8c9b2f-9089-4b76-979d-a8bdcebb0873",
                                  "debtId": "1b70ecc8-c24f-453f-9708-c46a27d9154f",
                                  "amount": 50.00,
                                  "idempotencyKey": "idem-1",
                                  "commandId": "cmd-1",
                                  "actor": "system"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createAllocation_shouldReturn404_whenProposalMissing() throws Exception {
        UUID proposalId = UUID.randomUUID();
        ExecuteAllocationCommand command = new ExecuteAllocationCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                proposalId,
                new BigDecimal("50.00"),
                "idem-1",
                "cmd-1",
                "system",
                Instant.parse("2026-06-01T10:00:00Z"));
        when(allocationWebMapper.toExecuteAllocationCommand(any(), any(Instant.class))).thenReturn(command);
        when(executeAllocationUseCase.executeAllocation(any())).thenThrow(new NotFoundWebException("Resource not found"));

        mockMvc.perform(post("/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "8f8c9b2f-9089-4b76-979d-a8bdcebb0873",
                                  "debtId": "1b70ecc8-c24f-453f-9708-c46a27d9154f",
                                  "proposalId": "%s",
                                  "amount": 50.00,
                                  "idempotencyKey": "idem-1",
                                  "commandId": "cmd-1",
                                  "actor": "system"
                                }
                                """.formatted(proposalId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAllocation_shouldReturn200_whenAllocationExists() throws Exception {
        UUID allocationId = UUID.randomUUID();
        UUID debtorId = UUID.randomUUID();
        Payment payment = Payment.received(
                UUID.randomUUID(),
                "TRX-001",
                new BigDecimal("20.00"),
                "EUR",
                null,
                null,
                null,
                null,
                Instant.parse("2026-06-01T10:00:00Z"));
        Debt debt = Debt.open(
                UUID.randomUUID(),
                debtorId,
                "D-001",
                new BigDecimal("20.00"),
                "EUR",
                null,
                Instant.parse("2026-06-01T10:00:00Z"));
        PaymentAllocation allocation = PaymentAllocation.execute(
                allocationId,
                payment,
                debt,
                null,
                new BigDecimal("20.00"),
                "idem-1",
                "cmd-1",
                "user",
                Instant.parse("2026-06-01T10:00:00Z"));
        AllocationResponse response = new AllocationResponse(
                allocationId,
                allocation.paymentId(),
                allocation.debtId(),
                null,
                new BigDecimal("20.00"),
                AllocationStatus.ALLOCATED,
                "idem-1",
                "cmd-1",
                "user",
                Instant.parse("2026-06-01T10:00:00Z"));

        when(getAllocationDetailUseCase.getAllocation(allocationId)).thenReturn(Optional.of(allocation));
        when(allocationWebMapper.toAllocationResponse(any())).thenReturn(response);

        mockMvc.perform(get("/allocations/{allocationId}", allocationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(allocationId.toString()));
    }

    @Test
    void getAllocation_shouldReturn404_whenAllocationMissing() throws Exception {
        UUID allocationId = UUID.randomUUID();
        when(getAllocationDetailUseCase.getAllocation(allocationId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/allocations/{allocationId}", allocationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    private Payment existingPayment(UUID paymentId) {
        return Payment.received(
                paymentId,
                "TRX-001",
                new BigDecimal("20.00"),
                "EUR",
                null,
                null,
                null,
                null,
                Instant.parse("2026-06-01T10:00:00Z"));
    }

    private Debt existingDebt(UUID debtId) {
        return Debt.open(
                debtId,
                UUID.randomUUID(),
                "D-001",
                new BigDecimal("20.00"),
                "EUR",
                null,
                Instant.parse("2026-06-01T10:00:00Z"));
    }
}
