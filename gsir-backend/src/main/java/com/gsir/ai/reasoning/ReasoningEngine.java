package com.gsir.ai.reasoning;

import java.util.List;

public interface ReasoningEngine {
    ReasoningResult evaluate(String context, String objective);
    Claim verifyClaim(Claim claim, List<Object> independentEvidence);
}
