package com.gsir.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MDC.clear();
    }

    @Test
    void testApiResponseWithMdcRequestId() {
        MDC.put("requestId", "req-test-123");
        ApiResponse<String> response = ApiResponse.ok("test-data");

        assertTrue(response.success());
        assertEquals("test-data", response.data());
        assertEquals("req-test-123", response.requestId());
        assertNotNull(response.timestamp());
    }

    @Test
    void testApiResponseWithExplicitRequestId() {
        ApiResponse<String> response = ApiResponse.ok("test-data", "req-explicit-999");

        assertTrue(response.success());
        assertEquals("test-data", response.data());
        assertEquals("req-explicit-999", response.requestId());
        assertNotNull(response.timestamp());
    }

    @Test
    void testApiResponseSerialization() throws Exception {
        ApiResponse<Map<String, String>> response = ApiResponse.ok(
                Map.of("key", "value"),
                "req-serialize-1"
        );

        String json = objectMapper.writeValueAsString(response);
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"data\":{\"key\":\"value\"}"));
        assertTrue(json.contains("\"requestId\":\"req-serialize-1\""));
        assertTrue(json.contains("\"timestamp\":"));
    }
}
