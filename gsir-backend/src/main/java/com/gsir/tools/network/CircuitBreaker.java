package com.gsir.tools.network;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class CircuitBreaker {

    private enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private State state = State.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final int failureThreshold;
    private final Duration openTimeout;
    private Instant lastFailureTime;

    public CircuitBreaker(int failureThreshold, Duration openTimeout) {
        this.failureThreshold = failureThreshold;
        this.openTimeout = openTimeout;
    }

    public synchronized <T> T execute(Supplier<T> action) {
        if (state == State.OPEN) {
            if (Instant.now().isAfter(lastFailureTime.plus(openTimeout))) {
                state = State.HALF_OPEN;
            } else {
                throw new CircuitBreakerOpenException("Circuit breaker is OPEN. Request rejected.");
            }
        }

        try {
            T result = action.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    private synchronized void onSuccess() {
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            failureCount.set(0);
        } else if (state == State.CLOSED) {
            failureCount.set(0);
        }
    }

    private synchronized void onFailure() {
        failureCount.incrementAndGet();
        if (state == State.HALF_OPEN || failureCount.get() >= failureThreshold) {
            state = State.OPEN;
            lastFailureTime = Instant.now();
        }
    }

    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
