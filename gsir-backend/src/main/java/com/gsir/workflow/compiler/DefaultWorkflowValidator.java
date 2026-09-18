package com.gsir.workflow.compiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.workflow.ResearchWorkflowEntity;
import com.gsir.workflow.ResearchWorkflowStepEntity;
import com.gsir.workflow.ResearchWorkflowStepRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DefaultWorkflowValidator implements WorkflowValidator {
    
    private final ResearchWorkflowStepRepository stepRepository;
    private final ObjectMapper objectMapper;

    public DefaultWorkflowValidator(ResearchWorkflowStepRepository stepRepository, ObjectMapper objectMapper) {
        this.stepRepository = stepRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean validate(ResearchWorkflowEntity workflow) {
        List<ResearchWorkflowStepEntity> steps = stepRepository.findByWorkflowId(workflow.getId());
        if (steps == null || steps.isEmpty()) {
            return true; // Empty DAG is valid trivially
        }

        Map<String, List<String>> adjList = new HashMap<>();
        Set<String> allNodeIds = new HashSet<>();

        for (ResearchWorkflowStepEntity step : steps) {
            String stepId = step.getId().toString();
            
            // Check duplicate nodes
            if (allNodeIds.contains(stepId)) {
                throw new IllegalStateException("Invalid DAG: Duplicate node ID detected: " + stepId);
            }
            allNodeIds.add(stepId);
            
            // Validate malformed configuration (inputs)
            validateJsonConfiguration(step.getInputs(), stepId);
            
            List<String> deps = parseDependencies(step.getDependencies(), stepId);
            for (String dep : deps) {
                if (dep.equals(stepId)) {
                    throw new IllegalStateException("Invalid DAG: Node " + stepId + " self-references.");
                }
            }
            adjList.put(stepId, deps);
        }

        // Check for missing dependencies
        for (Map.Entry<String, List<String>> entry : adjList.entrySet()) {
            for (String dep : entry.getValue()) {
                if (!allNodeIds.contains(dep)) {
                    throw new IllegalStateException("Invalid DAG: Missing dependency " + dep + " for node " + entry.getKey());
                }
            }
        }

        // Cycle Detection using Graph Coloring (0=unvisited, 1=visiting, 2=visited)
        Map<String, Integer> state = new HashMap<>();
        for (String node : allNodeIds) {
            state.put(node, 0);
        }

        for (String node : allNodeIds) {
            if (state.get(node) == 0) {
                if (hasCycle(node, adjList, state)) {
                    throw new IllegalStateException("Invalid DAG: Cycle detected involving node " + node);
                }
            }
        }

        return true;
    }

    private boolean hasCycle(String node, Map<String, List<String>> adjList, Map<String, Integer> state) {
        state.put(node, 1);
        List<String> neighbors = adjList.getOrDefault(node, Collections.emptyList());
        for (String neighbor : neighbors) {
            if (state.get(neighbor) == 1) {
                return true; // Cycle detected
            }
            if (state.get(neighbor) == 0 && hasCycle(neighbor, adjList, state)) {
                return true;
            }
        }
        state.put(node, 2);
        return false;
    }

    private List<String> parseDependencies(String depsJson, String stepId) {
        if (depsJson == null || depsJson.isBlank() || depsJson.equals("null")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(depsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Invalid DAG: Malformed dependencies for node " + stepId);
        }
    }
    
    private void validateJsonConfiguration(String configJson, String stepId) {
        if (configJson == null || configJson.isBlank() || configJson.equals("null")) {
            return;
        }
        try {
            objectMapper.readTree(configJson);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid DAG: Malformed configuration inputs for node " + stepId);
        }
    }
}
