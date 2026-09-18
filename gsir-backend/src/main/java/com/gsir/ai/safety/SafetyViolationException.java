package com.gsir.ai.safety;
import com.gsir.common.error.GsirException;
import org.springframework.http.HttpStatus;

public class SafetyViolationException extends GsirException {
    public SafetyViolationException(String message) {
        super("SAFETY_VIOLATION", message, HttpStatus.FORBIDDEN);
    }
}
