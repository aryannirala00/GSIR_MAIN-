package com.gsir.execution.service;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.execution.adapter.HumanReviewTaskRepository;
import com.gsir.execution.model.HumanReviewTaskEntity;
import com.gsir.jobs.JobService;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class HumanReviewService {

    private final HumanReviewTaskRepository repository;
    private final JobService jobService;
    private final UuidV7Generator idGenerator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HumanReviewService(HumanReviewTaskRepository repository, JobService jobService, UuidV7Generator idGenerator) {
        this.repository = repository;
        this.jobService = jobService;
        this.idGenerator = idGenerator;
    }

    public HumanReviewTaskEntity createReviewTask(UUID projectId, UUID workflowId, UUID stepId, Map<String, Object> context) {
        HumanReviewTaskEntity task = new HumanReviewTaskEntity(idGenerator.generate(), projectId, workflowId, stepId, context);
        return repository.save(task);
    }

    public HumanReviewTaskEntity resolveTask(UUID projectId, UUID taskId, UUID reviewerId, Map<String, Object> decision) {
        HumanReviewTaskEntity task = repository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found or access denied"));
                
        if (!"PENDING".equals(task.getStatus())) {
            throw new IllegalStateException("Task is already resolved");
        }
        
        task.resolve(reviewerId, decision);
        HumanReviewTaskEntity saved = repository.save(task);
        
        // Trigger workflow step evaluation by enqueuing a job
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("stepId", saved.getStepId().toString());
        jobService.submitJob(reviewerId, null, "WORKFLOW_STEP", payload.toString());
        
        return saved;
    }

    public List<HumanReviewTaskEntity> getProjectTasks(UUID projectId) {
        return repository.findByProjectId(projectId);
    }
}
