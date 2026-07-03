package com.pipelinepro.adapter.in.web.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalRestExceptionHandlerWebMvcTest.TestExceptionController.class)
@Import({GlobalRestExceptionHandler.class, GlobalRestExceptionHandlerWebMvcTest.TestExceptionController.class})
class GlobalRestExceptionHandlerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_return400ProblemDetail_when_validationFails() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void should_return400ProblemDetail_when_illegalArgumentExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad request"))
                .andExpect(jsonPath("$.path").value("/test/illegal-argument"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void should_return409ProblemDetail_when_illegalStateExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflict detected"))
                .andExpect(jsonPath("$.path").value("/test/illegal-state"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void should_return409ProblemDetail_when_illegalStateExceptionContainsNotFoundMessage() throws Exception {
        mockMvc.perform(get("/test/not-found-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflict detected"))
                .andExpect(jsonPath("$.path").value("/test/not-found-state"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void should_return400SanitizedProblemDetail_when_badRequestWebExceptionContainsSensitiveMessage() throws Exception {
        mockMvc.perform(get("/test/bad-request-web"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad request"))
                .andExpect(jsonPath("$.path").value("/test/bad-request-web"));
    }

    @Test
    void should_return404SanitizedProblemDetail_when_notFoundWebExceptionContainsSensitiveMessage() throws Exception {
        mockMvc.perform(get("/test/not-found-web"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found-web"));
    }

    @Test
    void should_return409SanitizedProblemDetail_when_conflictWebExceptionContainsSensitiveMessage() throws Exception {
        mockMvc.perform(get("/test/conflict-web"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflict detected"))
                .andExpect(jsonPath("$.path").value("/test/conflict-web"));
    }

    @Test
    void should_return403SanitizedProblemDetail_when_forbiddenWebExceptionContainsSensitiveMessage() throws Exception {
        mockMvc.perform(get("/test/forbidden-web"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Forbidden"))
                .andExpect(jsonPath("$.path").value("/test/forbidden-web"));
    }

    @Test
    void should_return403ProblemDetail_when_securityExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Forbidden"))
                .andExpect(jsonPath("$.path").value("/test/forbidden"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void should_returnGenericConflictMessage_when_illegalStateExceptionContainsSensitiveDetails() throws Exception {
        mockMvc.perform(get("/test/illegal-state-sensitive"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflict detected"));
    }

    @Test
    void should_returnForbiddenMessage_when_securityExceptionMessageIsBlank() throws Exception {
        mockMvc.perform(get("/test/forbidden-blank"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void should_returnBadRequestMessage_when_illegalArgumentExceptionMessageIsNull() throws Exception {
        mockMvc.perform(get("/test/illegal-argument-null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad request"));
    }

    @Test
    void should_return500ProblemDetail_when_unhandledExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test/unhandled"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.path").value("/test/unhandled"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @RestController
    public static class TestExceptionController {

        @PostMapping("/test/validation")
        void validate(@Valid @RequestBody TestRequest request) {
            // Validation is handled by Bean Validation annotations.
        }

        @GetMapping("/test/illegal-argument")
        void throwIllegalArgument() {
            throw new IllegalArgumentException("Invalid input");
        }

        @GetMapping("/test/illegal-state")
        void throwIllegalState() {
            throw new IllegalStateException("Conflict detected");
        }

        @GetMapping("/test/not-found-state")
        void throwNotFoundState() {
            throw new IllegalStateException("Resource not found");
        }

        @GetMapping("/test/forbidden")
        void throwForbidden() {
            throw new SecurityException("Forbidden access");
        }

        @GetMapping("/test/forbidden-blank")
        void throwForbiddenBlank() {
            throw new SecurityException("   ");
        }

        @GetMapping("/test/illegal-state-sensitive")
        void throwIllegalStateSensitive() {
            throw new IllegalStateException("SQL constraint UK_PAYMENT_INTERNAL failed");
        }

        @GetMapping("/test/illegal-argument-null")
        void throwIllegalArgumentNull() {
            throw new IllegalArgumentException((String) null);
        }

        @GetMapping("/test/bad-request-web")
        void throwBadRequestWeb() {
            throw new BadRequestWebException("SQL: select * from internal_users where token='secret'");
        }

        @GetMapping("/test/not-found-web")
        void throwNotFoundWeb() {
            throw new NotFoundWebException("Customer 123-SECRET not found in tenant PROD");
        }

        @GetMapping("/test/conflict-web")
        void throwConflictWeb() {
            throw new ConflictWebException("Version mismatch on table payment_allocation with id 42");
        }

        @GetMapping("/test/forbidden-web")
        void throwForbiddenWeb() {
            throw new ForbiddenWebException("Role ADMIN_INTERNAL missing for accountId=abc-123-secret");
        }

        @GetMapping("/test/unhandled")
        void throwUnhandled() {
            throw new RuntimeException("Boom");
        }
    }

    private record TestRequest(@NotBlank(message = "name is required") String name) {
    }
}
