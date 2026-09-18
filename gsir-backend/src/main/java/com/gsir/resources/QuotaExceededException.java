package com.gsir.resources;

import com.gsir.common.error.GsirException;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a project's resource quota (parallel jobs, compute, tokens, storage)
 * is exceeded. Integrated with the Part 39 GsirException / GlobalExceptionHandler
 * architecture so that it serialises as a structured ApiError with HTTP 429.
 */
public class QuotaExceededException extends GsirException {

    private final String quotaType;
    private final long current;
    private final long limit;

    public QuotaExceededException(String quotaType, long current, long limit) {
        super(
            "QUOTA_EXCEEDED",
            String.format("Project %s quota exceeded (%d/%d)", quotaType, current, limit),
            HttpStatus.TOO_MANY_REQUESTS
        );
        this.quotaType = quotaType;
        this.current = current;
        this.limit = limit;
    }

    public String getQuotaType() { return quotaType; }
    public long getCurrent() { return current; }
    public long getLimit() { return limit; }
}

