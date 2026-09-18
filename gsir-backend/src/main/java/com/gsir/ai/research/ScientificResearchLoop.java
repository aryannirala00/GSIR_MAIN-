package com.gsir.ai.research;

import java.util.UUID;

public interface ScientificResearchLoop {
    /**
     * Executes the AGI scientific research loop for a given research question.
     * Maps to PART 100 — AGI + SCIENTIFIC RESEARCH LOOP.
     */
    ResearchLoopState executeLoop(UUID projectId, UUID userId, String researchQuestion, int maxIterations);
}
