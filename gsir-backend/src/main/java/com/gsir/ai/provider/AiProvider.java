package com.gsir.ai.provider;

public interface AiProvider {
    AiResponse executePrompt(AiRequest request);
}

