package com.pipelinepro.bootstrap;

import com.pipelinepro.PipelineProApplication;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAllocationProposalRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataAuditEventRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataDebtorRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataIntakeRequestRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentAllocationRepository;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataPaymentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest(classes = PipelineProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class IntakeObservabilityTest {

    private final MockMvc mockMvc;

    private final MeterRegistry meterRegistry;

    private final SpringDataIntakeRequestRepository springDataIntakeRequestRepository;

    private final SpringDataDebtorRepository springDataDebtorRepository;

    private final SpringDataDebtRepository springDataDebtRepository;

    private final SpringDataPaymentAllocationRepository springDataPaymentAllocationRepository;

    private final SpringDataAllocationProposalRepository springDataAllocationProposalRepository;

    private final SpringDataAuditEventRepository springDataAuditEventRepository;

    private final SpringDataPaymentRepository springDataPaymentRepository;

    IntakeObservabilityTest(
            MockMvc mockMvc,
            MeterRegistry meterRegistry,
            SpringDataIntakeRequestRepository springDataIntakeRequestRepository,
            SpringDataDebtorRepository springDataDebtorRepository,
            SpringDataDebtRepository springDataDebtRepository,
            SpringDataPaymentAllocationRepository springDataPaymentAllocationRepository,
            SpringDataAllocationProposalRepository springDataAllocationProposalRepository,
            SpringDataAuditEventRepository springDataAuditEventRepository,
            SpringDataPaymentRepository springDataPaymentRepository) {
        this.mockMvc = mockMvc;
        this.meterRegistry = meterRegistry;
        this.springDataIntakeRequestRepository = springDataIntakeRequestRepository;
        this.springDataDebtorRepository = springDataDebtorRepository;
        this.springDataDebtRepository = springDataDebtRepository;
        this.springDataPaymentAllocationRepository = springDataPaymentAllocationRepository;
        this.springDataAllocationProposalRepository = springDataAllocationProposalRepository;
        this.springDataAuditEventRepository = springDataAuditEventRepository;
        this.springDataPaymentRepository = springDataPaymentRepository;
    }

    @AfterEach
    void cleanDatabase() {
        springDataAuditEventRepository.deleteAll();
        springDataPaymentAllocationRepository.deleteAll();
        springDataAllocationProposalRepository.deleteAll();
        springDataDebtRepository.deleteAll();
        springDataDebtorRepository.deleteAll();
        springDataIntakeRequestRepository.deleteAll();
        springDataPaymentRepository.deleteAll();
    }

    @Test
    void should_incrementCreateDebtorCounterAndEchoCorrelationId_when_headerIsSafe() throws Exception {
        double before = counterValue("create_debtor");
        String idempotencyKey = "idem-obs-" + UUID.randomUUID();

        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBTOR"))
                        .header("Idempotency-Key", idempotencyKey)
                        .header("X-Correlation-Id", "corr-obs-1")
                        .contentType("application/json")
                        .content("{\"debtorType\":\"ENTERPRISE\",\"displayName\":\"Obs Corp\",\"enterpriseNumber\":\"BE0123456789\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Correlation-Id", "corr-obs-1"));

        assertThat(counterValue("create_debtor")).isEqualTo(before + 1.0d);
    }

    @Test
    void should_incrementCreateDebtCounter_when_requestIsObserved() throws Exception {
        double before = counterValue("create_debt");

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBT"))
                        .header("Idempotency-Key", "idem-obs-" + UUID.randomUUID())
                        .header("X-Correlation-Id", "corr-obs-2")
                        .contentType("application/json")
                        .content("{\"reference\":\"D-OBS-1\",\"originalAmount\":10.00,\"currency\":\"EUR\",\"openingStatus\":\"OPEN\"}"))
                .andExpect(status().isBadRequest());

        assertThat(counterValue("create_debt")).isEqualTo(before + 1.0d);
    }

    @Test
    void should_notEchoCorrelationId_when_headerIsUnsafe() throws Exception {
        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBTOR"))
                        .header("Idempotency-Key", "idem-obs-" + UUID.randomUUID())
                        .header("X-Correlation-Id", " corr-unsafe ")
                        .contentType("application/json")
                        .content("{\"debtorType\":\"ENTERPRISE\",\"displayName\":\"Obs Unsafe Corp\",\"enterpriseNumber\":\"BE9876543210\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("X-Correlation-Id"));
    }

    @Test
    void should_replay_same_debtor_and_debt_when_same_idempotency_keys_are_reused() throws Exception {
        String debtorIdempotencyKey = "idem-obs-replay-debtor-" + UUID.randomUUID();
        String debtIdempotencyKey = "idem-obs-replay-debt-" + UUID.randomUUID();

        MvcResult firstDebtorResult = mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBTOR"))
                        .header("Idempotency-Key", debtorIdempotencyKey)
                        .header("X-Correlation-Id", "corr-obs-replay-1")
                        .contentType("application/json")
                        .content("{\"debtorType\":\"ENTERPRISE\",\"displayName\":\"Replay Corp\",\"enterpriseNumber\":\"BE1111222233\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult replayDebtorResult = mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBTOR"))
                        .header("Idempotency-Key", debtorIdempotencyKey)
                        .header("X-Correlation-Id", "corr-obs-replay-1")
                        .contentType("application/json")
                        .content("{\"debtorType\":\"ENTERPRISE\",\"displayName\":\"Replay Corp\",\"enterpriseNumber\":\"BE1111222233\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String debtorId = extractId(firstDebtorResult);
        assertThat(extractId(replayDebtorResult)).isEqualTo(debtorId);

        MvcResult firstDebtResult = mockMvc.perform(post("/debts")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBT"))
                        .header("Idempotency-Key", debtIdempotencyKey)
                        .header("X-Correlation-Id", "corr-obs-replay-2")
                        .contentType("application/json")
                        .content("{\"debtorId\":\"" + debtorId + "\",\"reference\":\"D-OBS-REPLAY-1\",\"originalAmount\":10.00,\"currency\":\"EUR\",\"openingStatus\":\"OPEN\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult replayDebtResult = mockMvc.perform(post("/debts")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBT"))
                        .header("Idempotency-Key", debtIdempotencyKey)
                        .header("X-Correlation-Id", "corr-obs-replay-2")
                        .contentType("application/json")
                        .content("{\"debtorId\":\"" + debtorId + "\",\"reference\":\"D-OBS-REPLAY-1\",\"originalAmount\":10.00,\"currency\":\"EUR\",\"openingStatus\":\"OPEN\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(extractId(replayDebtResult)).isEqualTo(extractId(firstDebtResult));
        assertThat(springDataDebtorRepository.count()).isEqualTo(1L);
        assertThat(springDataDebtRepository.count()).isEqualTo(1L);
        assertThat(springDataIntakeRequestRepository.count()).isEqualTo(2L);
    }

    @Test
    void should_keep_intake_flow_decoupled_from_allocation_tables_when_intake_requests_are_processed() throws Exception {
        MvcResult createDebtorResult = mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBTOR"))
                        .header("Idempotency-Key", "idem-obs-decoupling-debtor-1")
                        .header("X-Correlation-Id", "corr-obs-decoupling-1")
                        .contentType("application/json")
                        .content("{\"debtorType\":\"ENTERPRISE\",\"displayName\":\"Decoupled Corp\",\"enterpriseNumber\":\"BE3333444455\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String debtorId = extractId(createDebtorResult);

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBT"))
                        .header("Idempotency-Key", "idem-obs-decoupling-debt-1")
                        .header("X-Correlation-Id", "corr-obs-decoupling-2")
                        .contentType("application/json")
                        .content("{\"debtorId\":\"" + debtorId + "\",\"reference\":\"D-OBS-DECOUPLED-1\",\"originalAmount\":10.00,\"currency\":\"EUR\",\"openingStatus\":\"OPEN\"}"))
                .andExpect(status().isCreated());

        assertThat(springDataPaymentAllocationRepository.count()).isZero();
        assertThat(springDataAllocationProposalRepository.count()).isZero();
        assertThat(springDataAuditEventRepository.findAll())
                .noneMatch(event -> event.getEventType().contains("ALLOCATION") || event.getEventType().contains("ALLOCATED"));
    }

    @Test
    void should_record_status_tagged_intake_observations_when_requests_are_handled_for_slo_checks() throws Exception {
        mockMvc.perform(post("/debtors")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBTOR"))
                        .header("Idempotency-Key", "idem-obs-slo-1")
                        .header("X-Correlation-Id", "corr-obs-slo-1")
                        .contentType("application/json")
                        .content("{\"debtorType\":\"ENTERPRISE\",\"displayName\":\"Slo Corp\",\"enterpriseNumber\":\"BE7777888899\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/debts")
                        .with(csrf())
                        .with(user("tester").authorities(() -> "CREATE_DEBT"))
                        .header("Idempotency-Key", "idem-obs-slo-2")
                        .header("X-Correlation-Id", "corr-obs-slo-2")
                        .contentType("application/json")
                        .content("{\"reference\":\"D-SLO-1\",\"originalAmount\":10.00,\"currency\":\"EUR\",\"openingStatus\":\"OPEN\"}"))
                .andExpect(status().isBadRequest());

        Timer successTimer = meterRegistry.find("pipelinepro.intake.request")
                .tag("operation", "create_debtor")
                .tag("status", "201")
                .timer();
        Timer failureTimer = meterRegistry.find("pipelinepro.intake.request")
                .tag("operation", "create_debt")
                .tag("status", "400")
                .timer();

        assertThat(successTimer).isNotNull();
        assertThat(successTimer.count()).isGreaterThan(0);
        assertThat(failureTimer).isNotNull();
        assertThat(failureTimer.count()).isGreaterThan(0);
    }

    @Test
    void should_expose_health_probe_without_authentication_when_slo_checks_poll_actuator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private double counterValue(String operation) {
        Counter counter = meterRegistry.find("pipelinepro.intake.requests")
                .tag("operation", operation)
                .counter();
        return counter == null ? 0.0d : counter.count();
    }

    private static String extractId(MvcResult mvcResult) throws Exception {
        String responseBody = mvcResult.getResponse().getContentAsString();
        return responseBody.replaceFirst(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
