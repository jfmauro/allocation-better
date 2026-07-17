package com.pipelinepro.bootstrap;

import com.pipelinepro.PipelineProApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PipelineProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LocalAdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminShouldBeAuthorizedToProtectedEndpoints() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String enterpriseNumber = validEnterpriseNumber();
        MvcResult debtorResult = mockMvc.perform(MockMvcRequestBuilders.post("/debtors")
                        .with(httpBasic("admin", "admin"))
                        .header("Idempotency-Key", "local-debtor-" + suffix)
                        .header("X-Correlation-Id", "local-correlation-" + suffix)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"debtorType\":\"ENTERPRISE\"," +
                                "\"displayName\":\"Acme " + suffix + "\"," +
                                "\"enterpriseNumber\":\"" + enterpriseNumber + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String debtorId = debtorResult.getResponse().getContentAsString()
                .replaceFirst(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(MockMvcRequestBuilders.post("/debts")
                        .with(httpBasic("admin", "admin"))
                        .header("Idempotency-Key", "local-debt-" + suffix)
                        .header("X-Correlation-Id", "local-correlation-debt-" + suffix)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"debtorId\":\"" + debtorId + "\"," +
                                "\"reference\":\"D-LOCAL-" + suffix + "\"," +
                                "\"originalAmount\":10.0," +
                                "\"currency\":\"EUR\"," +
                                "\"openingStatus\":\"OPEN\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.post("/payments")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"bankTransactionReference\":\"BTX-LOCAL-1\"," +
                                "\"valueDate\":\"2026-01-01T10:00:00Z\"," +
                                "\"amount\":10.0," +
                                "\"currency\":\"EUR\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/accounting-entries")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }

    private String validEnterpriseNumber() {
        long firstEightValue = Math.floorMod(UUID.randomUUID().getMostSignificantBits(), 2_000_000L);
        String firstEightDigits = String.format("%08d", firstEightValue);
        int checksum = 97 - (Integer.parseInt(firstEightDigits) % 97);
        return "BE" + firstEightDigits + String.format("%02d", checksum);
    }
}
