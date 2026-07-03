package com.pipelinepro.bootstrap;

import com.pipelinepro.PipelineProApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PipelineProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class IntakeSecurityMappingContextTest {

    private final MockMvc mockMvc;

    IntakeSecurityMappingContextTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void should_returnForbidden_when_postDebtorsWithoutCreateDebtorPermission() throws Exception {
        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "VIEW_DEBTOR_MASTER_DATA"))
                        .header("Idempotency-Key", "idem-sec-1")
                        .header("X-Correlation-Id", "corr-sec-1")
                        .contentType("application/json")
                        .content("{\"debtorType\":\"ENTERPRISE\",\"displayName\":\"Acme\",\"enterpriseNumber\":\"BE0123456789\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_returnForbidden_when_getDebtorsWithoutViewDebtorMasterDataPermission() throws Exception {
        mockMvc.perform(get("/debtors")
                        .with(user("tester").authorities(() -> "CREATE_DEBTOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_returnForbidden_when_postDebtsWithoutCreateDebtPermission() throws Exception {
        UUID debtorId = UUID.randomUUID();
        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "VIEW_DEBT_MASTER_DATA"))
                        .header("Idempotency-Key", "idem-sec-2")
                        .header("X-Correlation-Id", "corr-sec-2")
                        .contentType("application/json")
                        .content("{\"debtorId\":\"" + debtorId + "\",\"reference\":\"D-SEC-1\",\"originalAmount\":10.00,\"currency\":\"EUR\",\"openingStatus\":\"OPEN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_returnForbidden_when_getDebtWithoutViewDebtMasterDataPermission() throws Exception {
        mockMvc.perform(get("/debts/{debtId}", UUID.randomUUID())
                        .with(user("tester").authorities(() -> "CREATE_DEBT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_returnCreated_when_postDebtorsWithCreateDebtorPermission() throws Exception {
        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBTOR"))
                        .header("Idempotency-Key", "idem-sec-ok-1")
                        .header("X-Correlation-Id", "corr-sec-ok-1")
                        .contentType("application/json")
                        .content("{\"debtorType\":\"ENTERPRISE\",\"displayName\":\"Acme Allowed\",\"enterpriseNumber\":\"BE1122334455\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void should_returnCreated_when_postDebtsWithCreateDebtPermissionAndExistingDebtor() throws Exception {
        MvcResult createDebtorResult = mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBTOR"))
                        .header("Idempotency-Key", "idem-sec-ok-2")
                        .header("X-Correlation-Id", "corr-sec-ok-2")
                        .contentType("application/json")
                        .content("{\"debtorType\":\"ENTERPRISE\",\"displayName\":\"Debtor For Debt\",\"enterpriseNumber\":\"BE5566778899\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String debtorResponseBody = createDebtorResult.getResponse().getContentAsString();
        String debtorIdValue = debtorResponseBody.replaceFirst(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
        UUID debtorId = UUID.fromString(debtorIdValue);

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBT"))
                        .header("Idempotency-Key", "idem-sec-ok-3")
                        .header("X-Correlation-Id", "corr-sec-ok-3")
                        .contentType("application/json")
                        .content("{\"debtorId\":\"" + debtorId + "\",\"reference\":\"D-SEC-OK-1\",\"originalAmount\":10.00,\"currency\":\"EUR\",\"openingStatus\":\"OPEN\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void should_returnNotFound_when_getDebtWithViewDebtMasterDataPermissionAndDebtMissing() throws Exception {
        mockMvc.perform(get("/debts/{debtId}", UUID.randomUUID())
                        .with(user("tester").authorities(() -> "VIEW_DEBT_MASTER_DATA")))
                .andExpect(status().isNotFound());
    }
}
