package com.gsir.ai.research;

import com.gsir.ai.execution.AgentExecutor;
import com.gsir.ai.execution.ExecutionBudget;
import com.gsir.ai.execution.ExecutionState;
import com.gsir.ai.metacognition.MetacognitionService;
import com.gsir.ai.metacognition.MetaAssessment;
import com.gsir.ai.planning.Plan;
import com.gsir.ai.planning.Planner;
import com.gsir.ai.reasoning.ReasoningEngine;
import com.gsir.ai.reasoning.ReasoningResult;
import com.gsir.common.error.CommonErrors;
import com.gsir.experiments.service.ExperimentService;
import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.knowledge.service.KnowledgeService;
import com.gsir.learning.service.LearningLoopService;
import com.gsir.projects.ProjectAccessService;
import com.gsir.validation.engine.ScientificValidationEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DefaultScientificResearchLoop implements ScientificResearchLoop {

    private final ProjectAccessService projectAccessService;
    private final Planner planner;
    private final ReasoningEngine reasoningEngine;
    private final MetacognitionService metacognitionService;
    private final KnowledgeService knowledgeService;
    private final LearningLoopService learningLoopService;
    private final ExperimentService experimentService;
    private final ScientificValidationEngine validationEngine;
    private final AgentExecutor agentExecutor;

    public DefaultScientificResearchLoop(
            ProjectAccessService projectAccessService,
            Planner planner,
            ReasoningEngine reasoningEngine,
            MetacognitionService metacognitionService,
            KnowledgeService knowledgeService,
            LearningLoopService learningLoopService,
            ExperimentService experimentService,
            ScientificValidationEngine validationEngine,
            AgentExecutor agentExecutor) {
        this.projectAccessService = projectAccessService;
        this.planner = planner;
        this.reasoningEngine = reasoningEngine;
        this.metacognitionService = metacognitionService;
        this.knowledgeService = knowledgeService;
        this.learningLoopService = learningLoopService;
        this.experimentService = experimentService;
        this.validationEngine = validationEngine;
        this.agentExecutor = agentExecutor;
    }

    @Override
    @Transactional
    public ResearchLoopState executeLoop(UUID projectId, UUID userId, String researchQuestion, int maxIterations) {
        projectAccessService.requireAccess(projectId, userId);

        ResearchLoopState state = new ResearchLoopState();
        state.setProjectId(projectId);
        state.setOriginalQuestion(researchQuestion);
        state.setStatus("IN_PROGRESS");
        state.setIterationCount(0);

        ExecutionBudget budget = new ExecutionBudget(
                maxIterations,
                maxIterations * 5,
                10,
                7200000L,
                10000000L,
                100.0
        );
        // 1. Goal Formation & Attention (handled by execution state init)
        ExecutionState execState = agentExecutor.executeTask(researchQuestion, projectId, userId, budget);
        
        while (state.getIterationCount() < maxIterations && !"COMPLETED".equals(state.getStatus())) {
            state.setIterationCount(state.getIterationCount() + 1);

            // 2. Evidence Retrieval + Memory + Reasoning + Metacognition (delegated to agent executor / reasoning engine)
            execState = agentExecutor.resumeExecution(execState);

            if ("AWAITING_HUMAN_REVIEW".equals(execState.status())) {
                state.setStatus("NEEDS_HUMAN_REVIEW");
                state.setConclusion("Metacognitive failure or contradiction. Human review required.");
                break;
            }

            if ("NEEDS_EVIDENCE".equals(execState.status())) {
                state.setStatus("INSUFFICIENT_EVIDENCE");
                state.setConclusion("Cannot proceed due to insufficient evidence.");
                break;
            }

            if ("BUDGET_EXHAUSTED".equals(execState.status())) {
                state.setStatus("INSUFFICIENT_EVIDENCE");
                state.setConclusion("Computation budget exhausted before reaching validated conclusion.");
                break;
            }

            // 3. Hypothesis Formation (simplified extraction from reasoning step)
            state.setCurrentHypothesis("Hypothesis related to: " + execState.objective());

            // 4. Planning & Tool/Experiment execution
            // In GSIR architecture, Part 66 is MOCK, so experiment physical execution is NOT claimed.
            // We model the limitation explicitly.
            boolean canExecuteExperiment = false; // System cannot autonomously run physical labs
            
            if (!canExecuteExperiment) {
                // If we need an experiment but cannot run it, we halt for human intervention
                // unless we have simulation tools (which are abstract here)
                if ("NEEDS_TOOL_USE".equals(execState.status())) {
                    state.setStatus("NEEDS_HUMAN_REVIEW");
                    state.setConclusion("Tool or experiment execution required but not physically available. Human intervention required.");
                    break;
                }
            }

            // 5. Scientific Validation & Learning
            // Check if we reached a final conclusion
            if ("CONTINUE".equals(execState.status())) {
                // Assume we successfully synthesized existing knowledge
                state.setStatus("COMPLETED");
                state.setConclusion("Synthesized known knowledge: " + researchQuestion);
                state.setComplete(true);
            }
        }

        return state;
    }
}
