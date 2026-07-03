package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.error.GlobalRestExceptionHandler;
import com.pipelinepro.adapter.in.web.error.NotFoundWebException;
import com.pipelinepro.adapter.in.web.mapper.ProposalWebMapper;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalCandidateResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationProposalResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationResultResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.ProposalStateResponse;
import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.ProposalStatus;
import com.pipelinepro.domain.port.in.GetProposalCandidatesUseCase;
import com.pipelinepro.domain.port.in.GetProposalDetailUseCase;
import com.pipelinepro.domain.port.in.ProposalLifecycleUseCase;
import com.pipelinepro.domain.port.in.QueryDebtUseCase;
import com.pipelinepro.domain.port.in.QueryDebtorUseCase;
import com.pipelinepro.domain.port.in.command.MarkUnmatchedCommand;
import com.pipelinepro.domain.port.in.command.RejectProposalCommand;
import com.pipelinepro.domain.port.in.command.RequestInvestigationCommand;
import com.pipelinepro.domain.port.in.command.SelectDebtCommand;
import com.pipelinepro.domain.port.in.command.ValidateProposalCommand;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AllocationProposalController.class)
@Import(GlobalRestExceptionHandler.class)
class AllocationProposalControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProposalLifecycleUseCase proposalLifecycleUseCase;

    @MockitoBean
    private GetProposalDetailUseCase getProposalDetailUseCase;

    @MockitoBean
    private GetProposalCandidatesUseCase getProposalCandidatesUseCase;

    @MockitoBean
    private QueryDebtUseCase queryDebtUseCase;

    @MockitoBean
    private QueryDebtorUseCase queryDebtorUseCase;

    @MockitoBean
    private ProposalWebMapper proposalWebMapper;

    @Test
    void getProposal_shouldReturn200_whenProposalExists() throws Exception {
        UUID proposalId = UUID.randomUUID();
        AllocationProposal proposal = AllocationProposal.proposed(
                proposalId,
                UUID.randomUUID(),
                com.pipelinepro.domain.MatchingMethod.IDENTIFIER,
                "candidate",
                Instant.parse("2026-06-01T10:00:00Z"));
        AllocationProposalResponse response = new AllocationProposalResponse(
                proposalId,
                UUID.randomUUID(),
                ProposalStatus.PROPOSED,
                com.pipelinepro.domain.MatchingMethod.IDENTIFIER,
                "candidate",
                null,
                null,
                null,
                List.of(new AllocationProposalCandidateResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        com.pipelinepro.domain.MatchConfidence.HIGH,
                        new BigDecimal("10.00"),
                        0,
                        null,
                        null)),
                0L,
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-01T10:00:00Z"));

        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(proposal));
        when(getProposalCandidatesUseCase.listCandidates(proposalId)).thenReturn(List.of());
        when(proposalWebMapper.toAllocationProposalResponseWithCandidateResponses(any(), anyList())).thenReturn(response);

        mockMvc.perform(get("/allocation-proposals/{proposalId}", proposalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(proposalId.toString()))
                .andExpect(jsonPath("$.candidates").isArray());
    }

    @Test
    void validateProposal_shouldReturn200_whenRequestIsValid() throws Exception {
        UUID proposalId = UUID.randomUUID();
        ValidateProposalCommand command = new ValidateProposalCommand(
                proposalId,
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                "agent",
                "Manual validation",
                Instant.parse("2026-06-01T10:00:00Z"));
        AllocationResultResponse response = new AllocationResultResponse(
                UUID.randomUUID(),
                proposalId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                "ALLOCATED",
                "agent",
                Instant.parse("2026-06-01T10:00:00Z"));

        when(proposalWebMapper.toValidateProposalCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(command);
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(existingProposal(proposalId)));
        when(proposalLifecycleUseCase.validateProposal(any())).thenReturn(null);
        when(proposalWebMapper.toAllocationResultResponse(any())).thenReturn(response);

        mockMvc.perform(post("/allocation-proposals/{proposalId}/validate", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtId": "a5f4cf4a-5789-47f2-a568-c9bdf91a3cdc",
                                  "amount": 50.00,
                                  "actor": "agent",
                                  "reason": "Manual validation"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allocationId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ALLOCATED"));
    }

    @Test
    void rejectProposal_shouldReturn200_whenRequestIsValid() throws Exception {
        UUID proposalId = UUID.randomUUID();
        RejectProposalCommand command = new RejectProposalCommand(proposalId, "agent", "wrong debtor", Instant.parse("2026-06-01T10:00:00Z"));
        ProposalStateResponse response = new ProposalStateResponse(
                proposalId,
                ProposalStatus.REJECTED,
                "wrong debtor",
                "agent",
                Instant.parse("2026-06-01T10:00:00Z"),
                null,
                Instant.parse("2026-06-01T10:00:00Z"));

        when(proposalWebMapper.toRejectProposalCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(command);
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(existingProposal(proposalId)));
        when(proposalLifecycleUseCase.rejectProposal(any())).thenReturn(null);
        when(proposalWebMapper.toProposalStateResponse(any())).thenReturn(response);

        mockMvc.perform(post("/allocation-proposals/{proposalId}/reject", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actor": "agent",
                                  "reason": "wrong debtor"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void selectDebt_shouldReturn200_whenRequestIsValid() throws Exception {
        UUID proposalId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();
        SelectDebtCommand command = new SelectDebtCommand(proposalId, debtId, "agent", Instant.parse("2026-06-01T10:00:00Z"));
        ProposalStateResponse response = new ProposalStateResponse(
                proposalId,
                ProposalStatus.PROPOSED,
                null,
                null,
                null,
                debtId,
                Instant.parse("2026-06-01T10:00:00Z"));

        when(proposalWebMapper.toSelectDebtCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(command);
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(existingProposal(proposalId)));
        when(proposalLifecycleUseCase.selectDebt(any())).thenReturn(null);
        when(proposalWebMapper.toProposalStateResponse(any())).thenReturn(response);

        mockMvc.perform(post("/allocation-proposals/{proposalId}/select-debt", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtId": "%s",
                                  "actor": "agent"
                                }
                                """.formatted(debtId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedDebtId").value(debtId.toString()));
    }

    @Test
    void markUnmatched_shouldReturn200_whenRequestIsValid() throws Exception {
        UUID proposalId = UUID.randomUUID();
        MarkUnmatchedCommand command = new MarkUnmatchedCommand(proposalId, "agent", "no debt", Instant.parse("2026-06-01T10:00:00Z"));
        ProposalStateResponse response = new ProposalStateResponse(
                proposalId,
                ProposalStatus.UNMATCHED,
                "no debt",
                "agent",
                Instant.parse("2026-06-01T10:00:00Z"),
                null,
                Instant.parse("2026-06-01T10:00:00Z"));

        when(proposalWebMapper.toMarkUnmatchedCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(command);
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(existingProposal(proposalId)));
        when(proposalLifecycleUseCase.markUnmatched(any())).thenReturn(null);
        when(proposalWebMapper.toProposalStateResponse(any())).thenReturn(response);

        mockMvc.perform(post("/allocation-proposals/{proposalId}/mark-unmatched", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actor": "agent",
                                  "reason": "no debt"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNMATCHED"));
    }

    @Test
    void requestInvestigation_shouldReturn200_whenRequestIsValid() throws Exception {
        UUID proposalId = UUID.randomUUID();
        RequestInvestigationCommand command = new RequestInvestigationCommand(proposalId, "agent", "needs review", Instant.parse("2026-06-01T10:00:00Z"));
        ProposalStateResponse response = new ProposalStateResponse(
                proposalId,
                ProposalStatus.INVESTIGATION_REQUESTED,
                "needs review",
                "agent",
                Instant.parse("2026-06-01T10:00:00Z"),
                null,
                Instant.parse("2026-06-01T10:00:00Z"));

        when(proposalWebMapper.toRequestInvestigationCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(command);
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(existingProposal(proposalId)));
        when(proposalLifecycleUseCase.requestInvestigation(any())).thenReturn(null);
        when(proposalWebMapper.toProposalStateResponse(any())).thenReturn(response);

        mockMvc.perform(post("/allocation-proposals/{proposalId}/request-investigation", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actor": "agent",
                                  "reason": "needs review"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVESTIGATION_REQUESTED"));
    }

    @Test
    void validateProposal_shouldReturn400ProblemDetail_whenValidationFails() throws Exception {
        UUID proposalId = UUID.randomUUID();

        mockMvc.perform(post("/allocation-proposals/{proposalId}/validate", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 0,
                                  "actor": "",
                                  "reason": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("debtId")));
    }

    @Test
    void getProposal_shouldReturn404_whenProposalMissing() throws Exception {
        UUID proposalId = UUID.randomUUID();
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/allocation-proposals/{proposalId}", proposalId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void validateProposal_shouldReturn403_whenSecurityExceptionIsThrown() throws Exception {
        UUID proposalId = UUID.randomUUID();
        when(proposalWebMapper.toValidateProposalCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(new ValidateProposalCommand(
                proposalId,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "agent",
                "validate",
                Instant.parse("2026-06-01T10:00:00Z")));
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(existingProposal(proposalId)));
        when(proposalLifecycleUseCase.validateProposal(any())).thenThrow(new SecurityException("Forbidden operation"));

        mockMvc.perform(post("/allocation-proposals/{proposalId}/validate", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtId": "a5f4cf4a-5789-47f2-a568-c9bdf91a3cdc",
                                  "amount": 20.00,
                                  "actor": "agent",
                                  "reason": "validate"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void validateProposal_shouldReturn409_whenIllegalStateConflictIsThrown() throws Exception {
        UUID proposalId = UUID.randomUUID();
        when(proposalWebMapper.toValidateProposalCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(new ValidateProposalCommand(
                proposalId,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "agent",
                "validate",
                Instant.parse("2026-06-01T10:00:00Z")));
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(existingProposal(proposalId)));
        when(proposalLifecycleUseCase.validateProposal(any())).thenThrow(new IllegalStateException("Proposal version conflict"));

        mockMvc.perform(post("/allocation-proposals/{proposalId}/validate", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtId": "a5f4cf4a-5789-47f2-a568-c9bdf91a3cdc",
                                  "amount": 20.00,
                                  "actor": "agent",
                                  "reason": "validate"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rejectProposal_shouldReturn404_whenProposalMissing() throws Exception {
        UUID proposalId = UUID.randomUUID();
        when(proposalWebMapper.toRejectProposalCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(new RejectProposalCommand(
                proposalId,
                "agent",
                "wrong",
                Instant.parse("2026-06-01T10:00:00Z")));
        when(proposalLifecycleUseCase.rejectProposal(any())).thenThrow(new NotFoundWebException("Resource not found"));

        mockMvc.perform(post("/allocation-proposals/{proposalId}/reject", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actor": "agent",
                                  "reason": "wrong"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void selectDebt_shouldReturn409_whenIllegalStateConflictIsThrown() throws Exception {
        UUID proposalId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();
        when(proposalWebMapper.toSelectDebtCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(new SelectDebtCommand(
                proposalId,
                debtId,
                "agent",
                Instant.parse("2026-06-01T10:00:00Z")));
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(existingProposal(proposalId)));
        when(proposalLifecycleUseCase.selectDebt(any())).thenThrow(new IllegalStateException("Proposal status conflict"));

        mockMvc.perform(post("/allocation-proposals/{proposalId}/select-debt", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtId": "%s",
                                  "actor": "agent"
                                }
                                """.formatted(debtId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void requestInvestigation_shouldReturn403_whenSecurityExceptionIsThrown() throws Exception {
        UUID proposalId = UUID.randomUUID();
        when(proposalWebMapper.toRequestInvestigationCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(new RequestInvestigationCommand(
                proposalId,
                "agent",
                "needs review",
                Instant.parse("2026-06-01T10:00:00Z")));
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.of(existingProposal(proposalId)));
        when(proposalLifecycleUseCase.requestInvestigation(any())).thenThrow(new SecurityException("Forbidden operation"));

        mockMvc.perform(post("/allocation-proposals/{proposalId}/request-investigation", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actor": "agent",
                                  "reason": "needs review"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void validateProposal_shouldReturn404_whenProposalMissing() throws Exception {
        UUID proposalId = UUID.randomUUID();
        when(proposalWebMapper.toValidateProposalCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(new ValidateProposalCommand(
                proposalId,
                UUID.randomUUID(),
                new BigDecimal("20.00"),
                "agent",
                "validate",
                Instant.parse("2026-06-01T10:00:00Z")));
        when(proposalLifecycleUseCase.validateProposal(any())).thenThrow(new NotFoundWebException("Resource not found"));

        mockMvc.perform(post("/allocation-proposals/{proposalId}/validate", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtId": "a5f4cf4a-5789-47f2-a568-c9bdf91a3cdc",
                                  "amount": 20.00,
                                  "actor": "agent",
                                  "reason": "validate"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void selectDebt_shouldReturn404_whenProposalMissing() throws Exception {
        UUID proposalId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();
        when(proposalWebMapper.toSelectDebtCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(new SelectDebtCommand(
                proposalId,
                debtId,
                "agent",
                Instant.parse("2026-06-01T10:00:00Z")));
        when(proposalLifecycleUseCase.selectDebt(any())).thenThrow(new NotFoundWebException("Resource not found"));

        mockMvc.perform(post("/allocation-proposals/{proposalId}/select-debt", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "debtId": "%s",
                                  "actor": "agent"
                                }
                                """.formatted(debtId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void markUnmatched_shouldReturn404_whenProposalMissing() throws Exception {
        UUID proposalId = UUID.randomUUID();
        when(proposalWebMapper.toMarkUnmatchedCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(new MarkUnmatchedCommand(
                proposalId,
                "agent",
                "no debt",
                Instant.parse("2026-06-01T10:00:00Z")));
        when(proposalLifecycleUseCase.markUnmatched(any())).thenThrow(new NotFoundWebException("Resource not found"));

        mockMvc.perform(post("/allocation-proposals/{proposalId}/mark-unmatched", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actor": "agent",
                                  "reason": "no debt"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void requestInvestigation_shouldReturn404_whenProposalMissing() throws Exception {
        UUID proposalId = UUID.randomUUID();
        when(proposalWebMapper.toRequestInvestigationCommand(any(UUID.class), any(), any(Instant.class))).thenReturn(new RequestInvestigationCommand(
                proposalId,
                "agent",
                "needs review",
                Instant.parse("2026-06-01T10:00:00Z")));
        when(proposalLifecycleUseCase.requestInvestigation(any())).thenThrow(new NotFoundWebException("Resource not found"));

        mockMvc.perform(post("/allocation-proposals/{proposalId}/request-investigation", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actor": "agent",
                                  "reason": "needs review"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private AllocationProposal existingProposal(UUID proposalId) {
        return AllocationProposal.proposed(
                proposalId,
                UUID.randomUUID(),
                com.pipelinepro.domain.MatchingMethod.NAME,
                "candidate",
                Instant.parse("2026-06-01T10:00:00Z"));
    }
}
