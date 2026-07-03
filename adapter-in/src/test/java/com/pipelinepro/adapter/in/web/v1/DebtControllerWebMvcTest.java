package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.error.GlobalRestExceptionHandler;
import com.pipelinepro.adapter.in.web.mapper.DebtWebMapper;
import com.pipelinepro.adapter.in.web.v1.dto.response.DebtListResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.DebtResponse;
import com.pipelinepro.adapter.in.SecurityConfig;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.DebtStatus;
import com.pipelinepro.domain.port.in.CreateDebtIntakeUseCase;
import com.pipelinepro.domain.port.in.QueryDebtUseCase;
import com.pipelinepro.domain.port.in.command.CreateDebtCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebtController.class)
@Import({GlobalRestExceptionHandler.class, SecurityConfig.class})
class DebtControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueryDebtUseCase queryDebtUseCase;

    @MockitoBean
    private CreateDebtIntakeUseCase createDebtIntakeUseCase;

    @MockitoBean
    private DebtWebMapper debtWebMapper;

    @Test
    @WithMockUser(authorities = "CREATE_DEBT")
    void createDebt_shouldReturn201_whenRequestAndHeadersAreValid() throws Exception {
        UUID debtId = UUID.randomUUID();
        UUID debtorId = UUID.randomUUID();
        Debt debt = Debt.open(debtId, debtorId, "D-001", new BigDecimal("100.00"), "EUR", null, Instant.parse("2026-06-01T10:00:00Z"));
        DebtResponse response = new DebtResponse(
                debtId,
                debtorId,
                new BigDecimal("100.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                null,
                "D-001",
                0L,
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-01T10:00:00Z"));

        when(debtWebMapper.toCreateDebtCommand(any(), any(), any())).thenReturn(new CreateDebtCommand(
                debtorId,
                "D-001",
                new BigDecimal("100.00"),
                "EUR",
                DebtStatus.OPEN,
                null,
                "idem-1",
                "corr-1"));
        when(createDebtIntakeUseCase.createDebt(any(CreateDebtCommand.class))).thenReturn(debt);
        when(debtWebMapper.toDebtResponse(any(Debt.class))).thenReturn(response);

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorId": "%s",
                                  "reference": "D-001",
                                  "originalAmount": 100.00,
                                  "currency": "EUR",
                                  "openingStatus": "OPEN"
                                }
                                """.formatted(debtorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(debtId.toString()));
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBT")
    void createDebt_shouldReturn400_whenIdempotencyKeyHeaderMissing() throws Exception {
        UUID debtorId = UUID.randomUUID();

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorId": "%s",
                                  "reference": "D-001",
                                  "originalAmount": 100.00,
                                  "currency": "EUR",
                                  "openingStatus": "OPEN"
                                }
                                """.formatted(debtorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(createDebtIntakeUseCase, debtWebMapper);
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBT")
    void createDebt_shouldReturn400_whenCorrelationIdHeaderBlank() throws Exception {
        UUID debtorId = UUID.randomUUID();

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .header("X-Correlation-Id", "   ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorId": "%s",
                                  "reference": "D-001",
                                  "originalAmount": 100.00,
                                  "currency": "EUR",
                                  "openingStatus": "OPEN"
                                }
                                """.formatted(debtorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(createDebtIntakeUseCase, debtWebMapper);
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBT")
    void createDebt_shouldReturn400_whenCorrelationIdHeaderMissing() throws Exception {
        UUID debtorId = UUID.randomUUID();

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorId": "%s",
                                  "reference": "D-001",
                                  "originalAmount": 100.00,
                                  "currency": "EUR",
                                  "openingStatus": "OPEN"
                                }
                                """.formatted(debtorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(createDebtIntakeUseCase, debtWebMapper);
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBT")
    void createDebt_shouldReturn400_whenIdempotencyKeyHeaderBlank() throws Exception {
        UUID debtorId = UUID.randomUUID();

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .header("Idempotency-Key", "   ")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorId": "%s",
                                  "reference": "D-001",
                                  "originalAmount": 100.00,
                                  "currency": "EUR",
                                  "openingStatus": "OPEN"
                                }
                                """.formatted(debtorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(createDebtIntakeUseCase, debtWebMapper);
    }

    @Test
    @WithMockUser(authorities = "VIEW_DEBT_MASTER_DATA")
    void createDebt_shouldReturn403_whenPermissionMissing() throws Exception {
        UUID debtorId = UUID.randomUUID();

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorId": "%s",
                                  "reference": "D-001",
                                  "originalAmount": 100.00,
                                  "currency": "EUR",
                                  "openingStatus": "OPEN"
                                }
                                """.formatted(debtorId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(createDebtIntakeUseCase, debtWebMapper);
    }

    @Test
    void createDebt_shouldReturn401_whenUnauthenticated() throws Exception {
        UUID debtorId = UUID.randomUUID();

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorId": "%s",
                                  "reference": "D-001",
                                  "originalAmount": 100.00,
                                  "currency": "EUR",
                                  "openingStatus": "OPEN"
                                }
                                """.formatted(debtorId)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(createDebtIntakeUseCase, debtWebMapper);
    }

    @Test
    @WithMockUser(authorities = "VIEW_DEBT_MASTER_DATA")
    void listDebtorDebts_shouldReturn200_whenDebtsExist() throws Exception {
        UUID debtorId = UUID.randomUUID();
        Debt debt = Debt.open(UUID.randomUUID(), debtorId, "D-001", new BigDecimal("100.00"), "EUR", null, Instant.parse("2026-06-01T10:00:00Z"));
        DebtListResponse response = new DebtListResponse(
                debtorId,
                List.of(new DebtResponse(
                        debt.id(),
                        debtorId,
                        new BigDecimal("100.00"),
                        "EUR",
                        DebtStatus.OPEN,
                        null,
                        null,
                        "D-001",
                        0L,
                        Instant.parse("2026-06-01T10:00:00Z"),
                        Instant.parse("2026-06-01T10:00:00Z"))));

        when(queryDebtUseCase.listDebtsByDebtor(any(UUID.class), any())).thenReturn(List.of(debt));
        when(debtWebMapper.toDebtListResponse(any(UUID.class), any())).thenReturn(response);

        mockMvc.perform(get("/debtors/{debtorId}/debts", debtorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debtorId").value(debtorId.toString()));
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBT")
    void listDebtorDebts_shouldReturn404_whenNoDebtsAndViewAuthorityIsMissing() throws Exception {
        UUID debtorId = UUID.randomUUID();
        when(queryDebtUseCase.listDebtsByDebtor(any(UUID.class), any())).thenReturn(List.of());

        mockMvc.perform(get("/debtors/{debtorId}/debts", debtorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listDebtorDebts_shouldReturn404_whenUnauthenticated() throws Exception {
        UUID debtorId = UUID.randomUUID();
        when(queryDebtUseCase.listDebtsByDebtor(any(UUID.class), any())).thenReturn(List.of());

        mockMvc.perform(get("/debtors/{debtorId}/debts", debtorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(authorities = "VIEW_DEBT_MASTER_DATA")
    void getDebt_shouldReturn404ProblemDetail_whenDebtIsMissing() throws Exception {
        UUID debtId = UUID.randomUUID();
        when(queryDebtUseCase.getDebt(debtId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/debts/{debtId}", debtId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.path").value("/debts/" + debtId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBT")
    void getDebt_shouldReturn404_whenViewAuthorityIsMissing() throws Exception {
        UUID debtId = UUID.randomUUID();
        when(queryDebtUseCase.getDebt(debtId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/debts/{debtId}", debtId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getDebt_shouldReturn404_whenUnauthenticated() throws Exception {
        UUID debtId = UUID.randomUUID();
        when(queryDebtUseCase.getDebt(debtId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/debts/{debtId}", debtId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
