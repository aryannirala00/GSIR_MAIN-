package com.gsir.common.error;

import com.gsir.common.web.RequestIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class}, excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {com.gsir.auth.JwtAuthenticationFilter.class}))
@Import({GlobalExceptionHandler.class, RequestIdFilter.class, GlobalExceptionHandlerTest.TestController.class})
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test/errors")
    static class TestController {

        record TestPayload(@NotBlank(message = "Title must not be blank") String title) {}

        @GetMapping("/not-found")
        void throwNotFound() {
            throw CommonErrors.notFound("Project");
        }

        @GetMapping("/forbidden")
        void throwForbidden() {
            throw CommonErrors.forbidden();
        }

        @GetMapping("/unauthorized")
        void throwUnauthorized() {
            throw CommonErrors.unauthorized();
        }

        @GetMapping("/conflict")
        void throwConflict() {
            throw CommonErrors.conflict("Project name already exists");
        }

        @GetMapping("/invalid")
        void throwInvalid() {
            throw CommonErrors.invalid("Query parameter is malformed");
        }

        @GetMapping("/unexpected")
        void throwUnexpected() {
            throw new RuntimeException("Unexpected database connectivity glitch");
        }

        @PostMapping("/validation")
        void validatePayload(@Valid @RequestBody TestPayload payload) {
        }
    }

    @Test
    @DisplayName("CommonErrors.notFound translates to 404 with RESOURCE_NOT_FOUND code")
    void shouldHandleNotFoundException() throws Exception {
        mockMvc.perform(get("/test/errors/not-found")
                        .header("X-Request-ID", "req-12345"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Request-ID", "req-12345"))
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Project was not found")))
                .andExpect(jsonPath("$.requestId", is("req-12345")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.fields", hasSize(0)));
    }

    @Test
    @DisplayName("CommonErrors.forbidden translates to 403 with FORBIDDEN code")
    void shouldHandleForbiddenException() throws Exception {
        mockMvc.perform(get("/test/errors/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")))
                .andExpect(jsonPath("$.message", is("You do not have permission to perform this operation")))
                .andExpect(jsonPath("$.requestId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.fields", hasSize(0)));
    }

    @Test
    @DisplayName("CommonErrors.unauthorized translates to 401 with UNAUTHORIZED code")
    void shouldHandleUnauthorizedException() throws Exception {
        mockMvc.perform(get("/test/errors/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")))
                .andExpect(jsonPath("$.message", is("Authentication is required")))
                .andExpect(jsonPath("$.requestId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.fields", hasSize(0)));
    }

    @Test
    @DisplayName("CommonErrors.conflict translates to 409 with CONFLICT code")
    void shouldHandleConflictException() throws Exception {
        mockMvc.perform(get("/test/errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")))
                .andExpect(jsonPath("$.message", is("Project name already exists")))
                .andExpect(jsonPath("$.requestId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.fields", hasSize(0)));
    }

    @Test
    @DisplayName("CommonErrors.invalid translates to 400 with INVALID_REQUEST code")
    void shouldHandleInvalidRequestException() throws Exception {
        mockMvc.perform(get("/test/errors/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")))
                .andExpect(jsonPath("$.message", is("Query parameter is malformed")))
                .andExpect(jsonPath("$.requestId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.fields", hasSize(0)));
    }

    @Test
    @DisplayName("Validation errors translate to 400 with VALIDATION_ERROR and structured fields")
    void shouldHandleValidationError() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", is("Request validation failed")))
                .andExpect(jsonPath("$.fields", hasSize(1)))
                .andExpect(jsonPath("$.fields[0].field", is("title")))
                .andExpect(jsonPath("$.fields[0].message", is("Title must not be blank")));
    }

    @Test
    @DisplayName("Malformed JSON payload translates to 400 with INVALID_REQUEST code")
    void shouldHandleMalformedJson() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")))
                .andExpect(jsonPath("$.message", is("Invalid request parameter or payload")))
                .andExpect(jsonPath("$.requestId", notNullValue()));
    }

    @Test
    @DisplayName("Unsupported HTTP method translates to 405 with INVALID_REQUEST code")
    void shouldHandleMethodNotSupported() throws Exception {
        mockMvc.perform(delete("/test/errors/validation"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")))
                .andExpect(jsonPath("$.message", is("HTTP method not supported")));
    }

    @Test
    @DisplayName("Unhandled exceptions translate to 500 with INTERNAL_ERROR code")
    void shouldHandleUnexpectedException() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")))
                .andExpect(jsonPath("$.message", is("An unexpected server error occurred")))
                .andExpect(jsonPath("$.requestId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.fields", hasSize(0)));
    }
}
