package com.gsir.ai.research;

import java.util.UUID;
import java.util.List;

public class ResearchLoopState {
    private UUID projectId;
    private String originalQuestion;
    private UUID goalId;
    private String currentHypothesis;
    private UUID planId;
    private UUID experimentId;
    private UUID validationId;
    private String conclusion;
    private boolean isComplete;
    private int iterationCount;
    private String status; // "IN_PROGRESS", "NEEDS_HUMAN_REVIEW", "INSUFFICIENT_EVIDENCE", "COMPLETED"

    // getters and setters
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getOriginalQuestion() { return originalQuestion; }
    public void setOriginalQuestion(String originalQuestion) { this.originalQuestion = originalQuestion; }

    public UUID getGoalId() { return goalId; }
    public void setGoalId(UUID goalId) { this.goalId = goalId; }

    public String getCurrentHypothesis() { return currentHypothesis; }
    public void setCurrentHypothesis(String currentHypothesis) { this.currentHypothesis = currentHypothesis; }

    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }

    public UUID getExperimentId() { return experimentId; }
    public void setExperimentId(UUID experimentId) { this.experimentId = experimentId; }

    public UUID getValidationId() { return validationId; }
    public void setValidationId(UUID validationId) { this.validationId = validationId; }

    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }

    public boolean isComplete() { return isComplete; }
    public void setComplete(boolean complete) { this.isComplete = complete; }

    public int getIterationCount() { return iterationCount; }
    public void setIterationCount(int iterationCount) { this.iterationCount = iterationCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
