package com.gsir.execution.service;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.execution.adapter.ExecutionCostRepository;
import com.gsir.execution.model.ExecutionCostEntity;
import com.gsir.projects.ProjectEntity;
import com.gsir.projects.ProjectRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;

@Service
public class CostTrackingService {

    private final ExecutionCostRepository repository;
    private final ProjectRepository projectRepository;
    private final UuidV7Generator idGenerator;

    public CostTrackingService(ExecutionCostRepository repository, ProjectRepository projectRepository, UuidV7Generator idGenerator) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.idGenerator = idGenerator;
    }

    public ExecutionCostEntity recordCost(UUID projectId, UUID workflowId, UUID stepId, Double compute, Double model, Double api, String currency) {
        ExecutionCostEntity cost = new ExecutionCostEntity(idGenerator.generate(), projectId, workflowId, stepId, compute, model, api, currency);
        return repository.save(cost);
    }

    public void enforceBudget(UUID projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
            
        if (project.getExecutionBudget() == null) {
            return; // No budget limit
        }
        
        List<ExecutionCostEntity> costs = repository.findByProjectId(projectId);
        double totalCost = costs.stream().mapToDouble(ExecutionCostEntity::getTotalCost).sum();
        
        if (totalCost >= project.getExecutionBudget()) {
            throw new IllegalStateException("Budget exceeded for project " + projectId + ". Total cost: " + totalCost + ", Budget: " + project.getExecutionBudget());
        }
    }

    public List<ExecutionCostEntity> getProjectCosts(UUID projectId) {
        return repository.findByProjectId(projectId);
    }
    
    public List<ExecutionCostEntity> getWorkflowCosts(UUID workflowId) {
        return repository.findByWorkflowId(workflowId);
    }
}
