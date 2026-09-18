package com.gsir.common.error;

import org.springframework.http.HttpStatus;

public class GsirException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public GsirException(
            String code,
            String message,
            HttpStatus status
    ) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

