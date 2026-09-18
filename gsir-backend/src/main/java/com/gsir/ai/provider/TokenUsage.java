package com.gsir.ai.provider;

public record TokenUsage(long inputTokens, long outputTokens, long totalTokens, boolean isAvailable) {
    public static TokenUsage unavailable() {
        return new TokenUsage(0, 0, 0, false);
    }
}

