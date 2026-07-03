package com.pipelinepro.bootstrap;

import com.pipelinepro.PipelineProApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PipelineProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PipelineProBootSmokeTest {

    @LocalServerPort
    private int port;

    @Test
    void applicationShouldStartAndServeStaticRouteAlias() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/app/smoke.txt"))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }

        assertThat(HttpStatus.valueOf(response.statusCode())).isEqualTo(HttpStatus.OK);
        assertThat(response.body()).contains("bootstrap-smoke");
    }
}
