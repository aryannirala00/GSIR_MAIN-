package com.gsir.ai.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Part90ActionSelectionTest {

    private DefaultActionSelector actionSelector;

    @BeforeEach
    void setUp() {
        actionSelector = new DefaultActionSelector();
    }

    @Test
    void testActionSelectionFormula() {
        // Candidates
        // Action 1: U = 100 - (2 * 50) - (1 * 10) = -10
        ActionCandidate action1 = new ActionCandidate("high_risk_tool", Map.of(), 100.0, 50.0, 10.0);
        // Action 2: U = 60 - (2 * 5) - (1 * 5) = 45
        ActionCandidate action2 = new ActionCandidate("safe_tool", Map.of(), 60.0, 5.0, 5.0);

        List<ActionCandidate> candidates = List.of(action1, action2);

        ActionCandidate selected = actionSelector.selectAction(candidates, 2.0, 1.0);

        assertNotNull(selected);
        assertEquals("safe_tool", selected.tool());
    }

    @Test
    void testDeterministicTieBreaking() {
        // Both have exact same score: U = 50 - 5 - 5 = 40
        ActionCandidate action1 = new ActionCandidate("tool_B", Map.of(), 50.0, 5.0, 5.0);
        ActionCandidate action2 = new ActionCandidate("tool_A", Map.of(), 50.0, 5.0, 5.0);

        ActionCandidate selected = actionSelector.selectAction(List.of(action1, action2), 1.0, 1.0);

        assertNotNull(selected);
        // "tool_A" and "tool_B" have same score. comparator orders ascending, but max() gets the "largest" element.
        // Wait: `thenComparing` uses natural string ordering. "tool_B" is > "tool_A". So max() will return "tool_B"!
        // Let's assert "tool_B" to prove deterministic ordering
        assertEquals("tool_B", selected.tool());
    }

    @Test
    void testSafetyThresholdRejectsExtremelyHighRisk() {
        // High risk (e.g. > 10.0) -> U = 100 - (0.1 * 20) - (1 * 0) = 98 (highest utility but extreme risk)
        ActionCandidate dangerousAction = new ActionCandidate("rm_rf", Map.of(), 100.0, 20.0, 0.0);

        ActionCandidate selected = actionSelector.selectAction(List.of(dangerousAction), 0.1, 1.0);

        // Should return null due to safety check preventing execution purely on score
        assertNull(selected);
    }

    @Test
    void testInvalidCandidatesFiltered() {
        ActionCandidate nanAction = new ActionCandidate("nan_tool", Map.of(), Double.NaN, 5.0, 5.0);
        ActionCandidate infAction = new ActionCandidate("inf_tool", Map.of(), 50.0, Double.POSITIVE_INFINITY, 5.0);
        ActionCandidate validAction = new ActionCandidate("valid_tool", Map.of(), 10.0, 1.0, 1.0);

        ActionCandidate selected = actionSelector.selectAction(List.of(nanAction, infAction, validAction), 1.0, 1.0);

        assertNotNull(selected);
        assertEquals("valid_tool", selected.tool());
    }

    @Test
    void testInvalidPolicyParametersThrows() {
        ActionCandidate action = new ActionCandidate("tool", Map.of(), 10.0, 1.0, 1.0);

        assertThrows(IllegalArgumentException.class, () -> {
            actionSelector.selectAction(List.of(action), Double.NaN, 1.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            actionSelector.selectAction(List.of(action), 1.0, Double.POSITIVE_INFINITY);
        });
    }

    @Test
    void testEmptyCandidates() {
        ActionCandidate selected = actionSelector.selectAction(List.of(), 1.0, 1.0);
        assertNull(selected);
        
        ActionCandidate nullSelected = actionSelector.selectAction(null, 1.0, 1.0);
        assertNull(nullSelected);
    }
}
