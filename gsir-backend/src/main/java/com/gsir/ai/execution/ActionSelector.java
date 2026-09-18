package com.gsir.ai.execution;

import java.util.List;
import java.util.UUID;

public interface ActionSelector {

    /**
     * Evaluates candidate actions using the policy score formula:
     * U(a) = B(a) - \lambda R(a) - \mu C(a)
     * where:
     * B(a) = expected benefit
     * R(a) = risk
     * C(a) = cost
     * \lambda, \mu = policy parameters
     *
     * @param candidates list of potential actions
     * @param lambda risk penalty parameter
     * @param mu cost penalty parameter
     * @return the selected action, or null if no action exceeds the safety/utility thresholds
     */
    ActionCandidate selectAction(List<ActionCandidate> candidates, double lambda, double mu);
}

