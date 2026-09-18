package com.gsir.ai.execution;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class DefaultActionSelector implements ActionSelector {

    @Override
    public ActionCandidate selectAction(List<ActionCandidate> candidates, double lambda, double mu) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        // Validate lambda and mu
        com.gsir.common.math.NumericSafety.requireFinite(lambda);
        com.gsir.common.math.NumericSafety.requireFinite(mu);

        // Calculate Utility Score: U(a) = B(a) - \lambda R(a) - \mu C(a)
        // Find the action with the maximum utility, with deterministic tie-breaking by tool name
        Comparator<ActionCandidate> comparator = Comparator
                .comparingDouble((ActionCandidate a) -> calculateUtility(a, lambda, mu))
                .thenComparing(ActionCandidate::tool);

        ActionCandidate selected = candidates.stream()
                .filter(this::isValid)
                .max(comparator)
                .orElse(null);

        // Safety policy check: Reject if risk is extremely high
        // Note: The arbitrary 10.0 threshold is an IMPLEMENTATION POLICY since the master document 
        // does not specify a specific numeric bound, but it requires that authorization/safety remain mandatory.
        if (selected != null && selected.risk() > 10.0) {
            return null;
        }

        return selected;
    }

    private boolean isValid(ActionCandidate action) {
        if (action == null) return false;
        return Double.isFinite(action.expectedUtility()) &&
               Double.isFinite(action.risk()) &&
               Double.isFinite(action.cost());
    }

    private double calculateUtility(ActionCandidate action, double lambda, double mu) {
        return action.expectedUtility() - (lambda * action.risk()) - (mu * action.cost());
    }
}
