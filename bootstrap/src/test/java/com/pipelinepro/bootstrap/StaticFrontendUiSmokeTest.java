package com.pipelinepro.bootstrap;

import com.pipelinepro.PipelineProApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PipelineProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StaticFrontendUiSmokeTest {

    @LocalServerPort
    private int port;

    @Test
    void should_serveDebtorAndDebtFrontendPages_withNavigationWiring() {
        HttpResponse<String> indexResponse = sendGet("/app/index.html");
        assertThat(HttpStatus.valueOf(indexResponse.statusCode())).isEqualTo(HttpStatus.OK);
        assertThat(indexResponse.body())
                .contains("/app/debtors/create.html")
                .contains("/app/debtors/list.html")
                .contains("/app/debts/create.html")
                .contains("/app/debts/search.html")
                .contains("/app/allocations/detail.html")
                .contains("/app/audit/access-log.html");

        assertPageContains("/app/debtors/create.html", "Debtor Intake", "/app/assets/js/debtors-create.js");
        assertPageContains("/app/debtors/list.html", "Debtor Master Data", "/app/assets/js/debtors-list.js");
        assertPageContains("/app/debts/create.html", "Debt Intake", "/app/assets/js/debts-create.js");
        assertPageContains("/app/debts/search.html", "Debt Search & Selection", "/app/assets/js/debts-search.js");
    }

    private void assertPageContains(String path, String headingText, String scriptPath) {
        HttpResponse<String> response = sendGet(path);
        assertThat(HttpStatus.valueOf(response.statusCode())).isEqualTo(HttpStatus.OK);
        assertThat(response.body())
                .contains(headingText)
                .contains(scriptPath)
                .contains("<main class=\"app-shell\" role=\"main\">");
    }

    private HttpResponse<String> sendGet(String path) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
