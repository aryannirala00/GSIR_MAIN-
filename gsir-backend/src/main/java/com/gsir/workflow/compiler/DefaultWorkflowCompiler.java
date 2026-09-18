package com.gsir.workflow.compiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.workflow.ResearchWorkflowEntity;
import com.gsir.workflow.ResearchWorkflowStepEntity;
import com.gsir.workflow.ResearchWorkflowStepRepository;
import com.gsir.workflow.model.WorkflowNode;
import com.gsir.workflow.model.WorkflowPlan;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DefaultWorkflowCompiler implements WorkflowCompiler {

    private final ResearchWorkflowStepRepository stepRepository;
    private final ObjectMapper objectMapper;

    public DefaultWorkflowCompiler(ResearchWorkflowStepRepository stepRepository, ObjectMapper objectMapper) {
        this.stepRepository = stepRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowPlan compile(ResearchWorkflowEntity workflow) {
        List<ResearchWorkflowStepEntity> steps = stepRepository.findByWorkflowId(workflow.getId());
        
        if (steps == null || steps.isEmpty()) {
            return new WorkflowPlan(
                Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()
            );
        }

        Map<String, List<String>> adjList = new HashMap<>(); // node -> its dependencies (parents)
        Map<String, List<String>> outgoingList = new HashMap<>(); // node -> its children
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, WorkflowNode> nodeMap = new HashMap<>();

        for (ResearchWorkflowStepEntity step : steps) {
            String id = step.getId().toString();
            outgoingList.putIfAbsent(id, new ArrayList<>());
            inDegree.putIfAbsent(id, 0);
            
            nodeMap.put(id, new WorkflowNode(
                id, step.getCapability(), step.getStepName(), parseJsonToMap(step.getInputs())
            ));

            List<String> deps = parseDependencies(step.getDependencies());
            adjList.put(id, deps);
            
            for (String dep : deps) {
                outgoingList.putIfAbsent(dep, new ArrayList<>());
                outgoingList.get(dep).add(id);
                inDegree.put(id, inDegree.getOrDefault(id, 0) + 1);
            }
        }

        // Topological Sort (Kahn's Algorithm) with Deterministic Ordering
        // Use PriorityQueue based on UUID strings to guarantee deterministic execution order
        PriorityQueue<String> queue = new PriorityQueue<>();
        List<WorkflowNode> runnableNodes = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
                runnableNodes.add(nodeMap.get(entry.getKey()));
            }
        }
        
        // Sort initial runnable nodes deterministically too
        runnableNodes.sort(Comparator.comparing(WorkflowNode::nodeId));

        List<String> executionOrder = new ArrayList<>();
        
        while (!queue.isEmpty()) {
            String u = queue.poll();
            executionOrder.add(u);
            
            for (String v : outgoingList.getOrDefault(u, Collections.emptyList())) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    queue.add(v);
                }
            }
        }

        return new WorkflowPlan(
            executionOrder,
            runnableNodes,
            adjList,
            Collections.emptyMap(), // Parameters resolution deferred to actual execution context (Part 69/70)
            Collections.emptyMap(), // Resource Requirements enforcement belongs to K8s/Execution engine
            Collections.emptyMap()  // Policies enforcement deferred to Security Engine
        );
    }

    private List<String> parseDependencies(String depsJson) {
        if (depsJson == null || depsJson.isBlank() || depsJson.equals("null")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(depsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isBlank() || json.equals("null")) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
