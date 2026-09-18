package com.gsir.common.api;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.UUID;

public record ApiResponse<T>(
        boolean success,
        T data,
        String requestId,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        String reqId = MDC.get("requestId");
        if (reqId == null || reqId.isBlank()) {
            reqId = UUID.randomUUID().toString();
        }
        return new ApiResponse<>(true, data, reqId, Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data, String requestId) {
        String reqId = (requestId != null && !requestId.isBlank()) ? requestId : MDC.get("requestId");
        if (reqId == null || reqId.isBlank()) {
            reqId = UUID.randomUUID().toString();
        }
        return new ApiResponse<>(true, data, reqId, Instant.now());
    }
}
