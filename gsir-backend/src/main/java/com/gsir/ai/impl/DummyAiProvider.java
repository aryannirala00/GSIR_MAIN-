package com.gsir.ai.impl;

import com.gsir.ai.provider.AiProvider;
import com.gsir.ai.provider.AiRequest;
import com.gsir.ai.provider.AiResponse;
import com.gsir.ai.provider.TokenUsage;
import org.springframework.stereotype.Component;

@Component
public class DummyAiProvider implements AiProvider {

    @Override
    public AiResponse executePrompt(AiRequest request) {
        // Dummy implementation simulating token usage
        int promptLength = request.prompt() == null ? 0 : request.prompt().length();
        long inputTokens = promptLength / 4;
        long outputTokens = 100;
        long totalTokens = inputTokens + outputTokens;
        
        // Simulating some model pricing e.g. $0.01 per 1k input, $0.03 per 1k output
        double cost = (inputTokens * 0.01 / 1000.0) + (outputTokens * 0.03 / 1000.0);
        
        return new AiResponse(
                "Simulated response to: " + request.prompt(),
                new TokenUsage(inputTokens, outputTokens, totalTokens, true),
                cost,
                true
        );
    }
}

