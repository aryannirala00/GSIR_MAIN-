package com.gsir.ai.planning;

public interface Planner {
    Plan createPlan(String objective, java.util.UUID projectId);
    Plan adaptPlan(Plan currentPlan, String newInformation);
}
