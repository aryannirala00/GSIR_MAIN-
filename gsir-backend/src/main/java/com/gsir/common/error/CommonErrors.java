package com.gsir.common.error;

import org.springframework.http.HttpStatus;

public final class CommonErrors {

    private CommonErrors() {}

    public static GsirException notFound(String resource) {
        return new GsirException(
                "RESOURCE_NOT_FOUND",
                resource + " was not found",
                HttpStatus.NOT_FOUND
        );
    }

    public static GsirException forbidden() {
        return new GsirException(
                "FORBIDDEN",
                "You do not have permission to perform this operation",
                HttpStatus.FORBIDDEN
        );
    }

    public static GsirException unauthorized() {
        return new GsirException(
                "UNAUTHORIZED",
                "Authentication is required",
                HttpStatus.UNAUTHORIZED
        );
    }

    public static GsirException conflict(String message) {
        return new GsirException(
                "CONFLICT",
                message,
                HttpStatus.CONFLICT
        );
    }

    public static GsirException invalid(String message) {
        return new GsirException(
                "INVALID_REQUEST",
                message,
                HttpStatus.BAD_REQUEST
        );
    }

    public static GsirException validation(String message) {
        return new GsirException(
                "VALIDATION_ERROR",
                message,
                HttpStatus.BAD_REQUEST
        );
    }

    public static GsirException internal(String message) {
        return new GsirException(
                "INTERNAL_ERROR",
                message,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    
    public static GsirException notImplemented(String message) {
        return new GsirException(
                "NOT_IMPLEMENTED",
                message + " is not implemented yet",
                HttpStatus.NOT_IMPLEMENTED
        );
    }
}
