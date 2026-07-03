package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.error.GlobalRestExceptionHandler;
import com.pipelinepro.adapter.in.web.mapper.DebtorWebMapper;
import com.pipelinepro.adapter.in.web.v1.dto.response.DebtorResponse;
import com.pipelinepro.adapter.in.SecurityConfig;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.DebtorType;
import com.pipelinepro.domain.port.in.CreateDebtorIntakeUseCase;
import com.pipelinepro.domain.port.in.QueryDebtorUseCase;
import com.pipelinepro.domain.port.in.command.CreateDebtorCommand;
import com.pipelinepro.domain.port.in.command.DebtorSearchCriteria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.verifyNoInteractions;

@WebMvcTest(DebtorController.class)
@Import({GlobalRestExceptionHandler.class, SecurityConfig.class})
class DebtorControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateDebtorIntakeUseCase createDebtorIntakeUseCase;

    @MockitoBean
    private QueryDebtorUseCase queryDebtorUseCase;

    @MockitoBean
    private DebtorWebMapper debtorWebMapper;

    @Test
    @WithMockUser(authorities = "CREATE_DEBTOR")
    void createDebtor_shouldReturn201_whenRequestAndHeadersAreValid() throws Exception {
        UUID debtorId = UUID.randomUUID();
        Debtor debtor = Debtor.activeEnterprise(debtorId, "Acme", "BE0123456789", Instant.parse("2026-06-01T10:00:00Z"));
        DebtorResponse response = new DebtorResponse(
                debtorId,
                DebtorType.ENTERPRISE,
                "Acme",
                null,
                "BE0123456789",
                true,
                Instant.parse("2026-06-01T10:00:00Z"));

        when(debtorWebMapper.toCreateDebtorCommand(any(), any(), any())).thenReturn(new CreateDebtorCommand(
                DebtorType.ENTERPRISE,
                "Acme",
                null,
                "BE0123456789",
                "idem-1",
                "corr-1"));
        when(createDebtorIntakeUseCase.createDebtor(any(CreateDebtorCommand.class))).thenReturn(debtor);
        when(debtorWebMapper.toDebtorResponse(any(Debtor.class))).thenReturn(response);

        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorType": "ENTERPRISE",
                                  "displayName": "Acme",
                                  "nationalNumber": null,
                                  "enterpriseNumber": "BE0123456789"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(debtorId.toString()));
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBTOR")
    void createDebtor_shouldReturn400_whenIdempotencyKeyHeaderMissing() throws Exception {
        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorType": "ENTERPRISE",
                                  "displayName": "Acme",
                                  "enterpriseNumber": "BE0123456789"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(createDebtorIntakeUseCase);
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBTOR")
    void createDebtor_shouldReturn400_whenCorrelationIdHeaderBlank() throws Exception {
        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .header("X-Correlation-Id", "   ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorType": "ENTERPRISE",
                                  "displayName": "Acme",
                                  "enterpriseNumber": "BE0123456789"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(createDebtorIntakeUseCase);
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBTOR")
    void createDebtor_shouldReturn400_whenCorrelationIdHeaderMissing() throws Exception {
        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorType": "ENTERPRISE",
                                  "displayName": "Acme",
                                  "enterpriseNumber": "BE0123456789"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(createDebtorIntakeUseCase);
    }

    @Test
    @WithMockUser(authorities = "VIEW_DEBTOR_MASTER_DATA")
    void createDebtor_shouldReturn403_whenPermissionMissing() throws Exception {
        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorType": "ENTERPRISE",
                                  "displayName": "Acme",
                                  "enterpriseNumber": "BE0123456789"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(createDebtorIntakeUseCase);
    }

    @Test
    void createDebtor_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .header("Idempotency-Key", "idem-1")
                        .header("X-Correlation-Id", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtorType": "ENTERPRISE",
                                  "displayName": "Acme",
                                  "enterpriseNumber": "BE0123456789"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(createDebtorIntakeUseCase);
    }

    @Test
    @WithMockUser(authorities = "VIEW_DEBTOR_MASTER_DATA")
    void listDebtors_shouldReturn200_whenQueryIsValid() throws Exception {
        UUID debtorId = UUID.randomUUID();
        Debtor debtor = Debtor.activeNaturalPerson(debtorId, "Alice", "85073003328", Instant.parse("2026-06-01T10:00:00Z"));
        DebtorResponse response = new DebtorResponse(debtorId, DebtorType.NATURAL_PERSON, "Alice", "85073003328", null, true, Instant.parse("2026-06-01T10:00:00Z"));
        when(queryDebtorUseCase.listDebtors(any(DebtorSearchCriteria.class))).thenReturn(java.util.List.of(debtor));
        when(debtorWebMapper.toDebtorResponse(any(Debtor.class))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/debtors")
                        .param("query", "Alice")
                        .param("debtorType", "NATURAL_PERSON")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nationalNumber").value("85073003328"));
    }
}
