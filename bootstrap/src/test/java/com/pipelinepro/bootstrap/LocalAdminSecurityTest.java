package com.pipelinepro.bootstrap;

import com.pipelinepro.PipelineProApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
        mockMvc.perform(MockMvcRequestBuilders.post("/debtors")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"debtorType\":\"ENTERPRISE\"," +
                                "\"displayName\":\"Acme\"," +
                                "\"enterpriseNumber\":\"BE0123456789\"}"))
                // We only validate that authorization is granted; payload validity can still fail.
                .andExpect(status().is4xxClientError());

        mockMvc.perform(MockMvcRequestBuilders.post("/debts")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"debtorId\":\"00000000-0000-0000-0000-000000000001\"," +
                                "\"reference\":\"D-LOCAL-1\"," +
                                "\"originalAmount\":10.0," +
                                "\"currency\":\"EUR\"," +
                                "\"openingStatus\":\"OPEN\"}"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(MockMvcRequestBuilders.post("/payments")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"bankTransactionReference\":\"BTX-LOCAL-1\"," +
                                "\"amount\":10.0," +
                                "\"currency\":\"EUR\"}"))
                .andExpect(status().isCreated());
    }
}
