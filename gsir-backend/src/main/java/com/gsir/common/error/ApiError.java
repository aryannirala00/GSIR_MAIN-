package com.gsir.common.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        String code,
        String message,
        String requestId,
        Instant timestamp,
        List<FieldError> fields
) {
    public record FieldError(
            String field,
            String message
    ) {}
}

