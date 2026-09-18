package com.gsir.ai.provider;

public record AiResponse(String content, TokenUsage tokenUsage, double estimatedCost, boolean isCostKnown) {}

