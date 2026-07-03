package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.SecurityConfig;
import com.pipelinepro.adapter.in.web.error.GlobalRestExceptionHandler;
import com.pipelinepro.adapter.in.web.mapper.DebtWebMapper;
import com.pipelinepro.adapter.in.web.mapper.PaymentWebMapper;
import com.pipelinepro.adapter.in.web.mapper.ProposalWebMapper;
import com.pipelinepro.domain.port.in.CreateDebtIntakeUseCase;
import com.pipelinepro.domain.port.in.GetProposalCandidatesUseCase;
import com.pipelinepro.domain.port.in.GetProposalDetailUseCase;
import com.pipelinepro.domain.port.in.ProposalLifecycleUseCase;
import com.pipelinepro.domain.port.in.QueryDebtUseCase;
import com.pipelinepro.domain.port.in.QueryDebtorUseCase;
import com.pipelinepro.domain.port.in.QueryPaymentUseCase;
import com.pipelinepro.domain.port.in.ReceivePaymentUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({PaymentController.class, AllocationProposalController.class, DebtController.class})
@Import({GlobalRestExceptionHandler.class, SecurityConfig.class})
class AdapterInSecurityTest {

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
    private CreateDebtIntakeUseCase createDebtIntakeUseCase;

    @MockitoBean
    private DebtWebMapper debtWebMapper;

    @Test
    void getPayment_shouldRemainPublic_whenUnauthenticated() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(queryPaymentUseCase.getPayment(paymentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/payments/{paymentId}", paymentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProposal_shouldRemainPublic_whenUnauthenticated() throws Exception {
        UUID proposalId = UUID.randomUUID();
        when(getProposalDetailUseCase.getProposal(proposalId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/allocation-proposals/{proposalId}", proposalId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDebt_shouldRemainPublic_whenUnauthenticated() throws Exception {
        UUID debtId = UUID.randomUUID();
        when(queryDebtUseCase.getDebt(debtId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/debts/{debtId}", debtId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "CREATE_DEBT")
    void getDebt_shouldRemainAccessible_whenViewAuthorityIsMissing() throws Exception {
        UUID debtId = UUID.randomUUID();
        when(queryDebtUseCase.getDebt(debtId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/debts/{debtId}", debtId))
                .andExpect(status().isNotFound());
    }
}
