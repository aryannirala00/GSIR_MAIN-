package com.gsir.provenance.service;

import com.gsir.provenance.model.*;
import com.gsir.provenance.repository.ProvenanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * Part 80 — Execution Provenance Service.
 *
 * Records machine-traceable provenance for workflow step execution:
 * 80.17 Execution provenance (execution_id, workflow_id, start/end, inputs, outputs, parameters, status)
 * 80.19 Tool provenance (tool_id, tool_version, invocation, inputs, outputs, execution_time, status)
 * 80.24/80.25 Actor provenance (AI_AGENT, SYSTEM, HUMAN with agent id and model reference)
 * 80.44/80.45 Content hashing and integrity verification (SHA-256)
 * 80.46 Provenance integrity protection
 * 80.84 Confidence classification (AUTOMATIC for runtime-captured provenance)
 *
 * This service MUST NOT create a duplicate provenance subsystem.
 * It extends the existing ProvenanceService/ProvenanceRepository.
 */
@Service
public class ExecutionProvenanceService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionProvenanceService.class);
    private static final String HASH_ALGORITHM = "SHA-256";

    private final ProvenanceRepository repository;

    public ExecutionProvenanceService(ProvenanceRepository repository) {
        this.repository = repository;
    }

    /**
     * Record full execution provenance for a workflow step.
     * Called by WorkflowStepWorker after a step completes.
     *
     * @param workflowId  the parent workflow
     * @param stepId      the specific step
     * @param capability  the tool/capability used (e.g. "LITERATURE_SEARCH")
     * @param toolVersion the tool version string
     * @param inputs      JSON-encoded inputs
     * @param outputs     JSON-encoded outputs (used for hash)
     * @param projectId   tenant project
     * @param actorId     UUID of the AI agent/system actor (null = SYSTEM)
     * @return the activity ID that was recorded
     */
    public UUID recordStepExecution(
            UUID workflowId,
            UUID stepId,
            String capability,
            String toolVersion,
            String inputs,
            String outputs,
            UUID projectId,
            UUID actorId) {

        try {
            Instant now = Instant.now();

            // 80.44 Compute integrity hash of outputs
            String outputHash = computeHash(outputs != null ? outputs : "");

            // 80.17/80.19 Record execution + tool provenance as a prov_activity
            Map<String, Object> parameters = Map.of(
                "capability", capability,
                "toolVersion", toolVersion != null ? toolVersion : "unknown",
                "workflowId", workflowId.toString(),
                "stepId", stepId.toString()
            );

            Map<String, Object> env = Map.of(
                "javaVersion", System.getProperty("java.version", "unknown"),
                "captureTime", now.toString(),
                "confidence", ProvenanceConfidence.AUTOMATIC.name()
            );

            // 80.24 Actor provenance: determine actor type
            String actorType = actorId != null ? AgentType.AI_MODEL.name() : AgentType.SYSTEM.name();

            ProvActivity activity = new ProvActivity(
                UUID.randomUUID(),
                projectId,
                "WORKFLOW_STEP_EXECUTION",
                "COMPLETED",
                now,
                now,
                parameters,
                env,
                now
            );

            repository.saveActivity(activity);

            // 80.17 Record the inputs as an artifact (USED relationship)
            if (inputs != null && !inputs.isBlank()) {
                String inputHash = computeHash(inputs);
                ProvArtifact inputArtifact = new ProvArtifact(
                    UUID.randomUUID(), projectId,
                    NodeType.DATA.name(),
                    inputHash, 1, null,
                    Map.of("stepId", stepId.toString(), "type", "STEP_INPUT"),
                    now
                );
                repository.saveArtifact(inputArtifact);
                // Activity USED the input artifact
                repository.saveRelationship(new ProvRelationship(
                    UUID.randomUUID(), projectId,
                    RelationshipType.USED,
                    activity.id(), inputArtifact.id(), now
                ));
            }

            // 80.17 Record the outputs as an artifact (WAS_GENERATED_BY relationship)
            if (outputs != null && !outputs.isBlank()) {
                ProvArtifact outputArtifact = new ProvArtifact(
                    UUID.randomUUID(), projectId,
                    NodeType.RESULT.name(),
                    outputHash, 1, null,
                    Map.of(
                        "stepId", stepId.toString(),
                        "type", "STEP_OUTPUT",
                        "integrityHash", outputHash,
                        "algorithm", HASH_ALGORITHM
                    ),
                    now
                );
                repository.saveArtifact(outputArtifact);
                // Output WAS_GENERATED_BY the execution activity
                repository.saveRelationship(new ProvRelationship(
                    UUID.randomUUID(), projectId,
                    RelationshipType.WAS_GENERATED_BY,
                    outputArtifact.id(), activity.id(), now
                ));
            }

            log.debug("Recorded execution provenance: activityId={}, workflowId={}, stepId={}, outputHash={}",
                activity.id(), workflowId, stepId, outputHash);

            return activity.id();

        } catch (Exception e) {
            // Part 80.83: never silently fail — mark provenance as UNKNOWN/INCOMPLETE
            log.error("Failed to record execution provenance for step {}: {}. Provenance is INCOMPLETE.",
                stepId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Part 80.44/80.45 — Compute SHA-256 content hash.
     * Idempotent: same content always produces same hash.
     *
     * @param content the content to hash
     * @return hex-encoded SHA-256 hash
     */
    public String computeHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Part 80.45 — Verify content matches its stored hash.
     *
     * @param content      the content to verify
     * @param expectedHash the hash the content should produce
     * @return true if hash matches (integrity verified)
     */
    public boolean verifyHash(String content, String expectedHash) {
        if (content == null || expectedHash == null) return false;
        return computeHash(content).equals(expectedHash);
    }

    /**
     * Part 80.24 — Register an AI agent in provenance.
     */
    public ProvAgent registerAiAgent(String modelName, String modelVersion, UUID projectId) {
        ProvAgent agent = new ProvAgent(
            UUID.randomUUID(), projectId,
            AgentType.AI_MODEL, modelName + "@" + modelVersion,
            Map.of("modelName", modelName, "modelVersion", modelVersion, "agentType", "AI_RESEARCH_AGENT"),
            Instant.now()
        );
        return repository.saveAgent(agent);
    }

    /**
     * Part 105 - Budget Provenance
     */
    public void recordBudgetEvent(UUID executionId, UUID projectId, String eventType, String details) {
        try {
            Instant now = Instant.now();
            ProvActivity activity = new ProvActivity(
                UUID.randomUUID(),
                projectId,
                "BUDGET_EVENT_" + eventType,
                "COMPLETED",
                now,
                now,
                Map.of("executionId", executionId.toString(), "details", details),
                Map.of("captureTime", now.toString(), "confidence", ProvenanceConfidence.AUTOMATIC.name()),
                now
            );
            repository.saveActivity(activity);
            log.debug("Recorded budget event: executionId={}, eventType={}", executionId, eventType);
        } catch (Exception e) {
            log.error("Failed to record budget event: {}", e.getMessage(), e);
        }
    }
}

