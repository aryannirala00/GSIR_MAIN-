package com.gsir.ai.safety;

public class RequireHumanReviewException extends RuntimeException {
    public RequireHumanReviewException(String message) {
        super(message);
    }
}
