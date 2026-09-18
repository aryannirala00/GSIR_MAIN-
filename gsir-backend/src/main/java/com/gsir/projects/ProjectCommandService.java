package com.gsir.projects;

import com.gsir.common.error.CommonErrors;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.common.idempotency.IdempotencyRecordEntity;
import com.gsir.common.idempotency.IdempotencyRecordId;
import com.gsir.common.idempotency.IdempotencyRecordRepository;
import com.gsir.outbox.OutboxEvent;
import com.gsir.outbox.OutboxEventRepository;
import com.gsir.workspaces.WorkspaceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Canonical write-side service for Project operations (Volume II Part 13 & 14).
 */
@Service
public class ProjectCommandService {

    private final ProjectRepository projectRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final WorkspaceRepository workspaceRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final UuidV7Generator idGenerator;
    private final TransactionTemplate transactionTemplate;

    public ProjectCommandService(
            ProjectRepository projectRepository,
            OutboxEventRepository outboxEventRepository,
            WorkspaceRepository workspaceRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            UuidV7Generator idGenerator,
            TransactionTemplate transactionTemplate) {
        this.projectRepository = projectRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.workspaceRepository = workspaceRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.idGenerator = idGenerator;
        this.transactionTemplate = transactionTemplate;
    }

    public ProjectEntity createProject(UUID userId, String idempotencyKey, CreateProjectCommand command) {
        if (userId == null || command == null || command.workspaceId() == null) {
            throw CommonErrors.invalid("userId, command, and workspaceId must not be null");
        }
        if (command.name() == null || command.name().isBlank()) {
            throw CommonErrors.invalid("Project name must not be blank");
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecordId recordId = new IdempotencyRecordId(userId, idempotencyKey);
            Optional<IdempotencyRecordEntity> existingRecord = idempotencyRecordRepository.findById(recordId);
            if (existingRecord.isPresent()) {
                return projectRepository.findById(existingRecord.get().getResourceId())
                        .orElseThrow(() -> CommonErrors.notFound("Project"));
            }
        }

        return executeCreate(userId, idempotencyKey, command);
    }

    private ProjectEntity executeCreate(UUID userId, String idempotencyKey, CreateProjectCommand command) {
        try {
            return transactionTemplate.execute(status -> {
                workspaceRepository.findAccessibleWorkspace(command.workspaceId(), userId)
                        .orElseThrow(() -> CommonErrors.notFound("Workspace"));

                UUID projectId = idGenerator.generate();

                // Insert idempotency record FIRST as the serialization point.
                // The PK (user_id, idempotency_key) unique constraint ensures only
                // one transaction succeeds. saveAndFlush forces the SQL INSERT
                // immediately so the DB's row lock blocks the competing thread
                // before any project or outbox record is created.
                if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                    idempotencyRecordRepository.saveAndFlush(
                            new IdempotencyRecordEntity(new IdempotencyRecordId(userId, idempotencyKey), "Project", projectId)
                    );
                }

                ProjectEntity project = projectRepository.save(
                        new ProjectEntity(projectId, command.workspaceId(), command.name(), command.description())
                );

                outboxEventRepository.save(
                        OutboxEvent.projectCreated(idGenerator.generate(), project)
                );

                return project;
            });

        } catch (DataIntegrityViolationException race) {
            if (idempotencyKey != null) {
                return idempotencyRecordRepository
                        .findById(new IdempotencyRecordId(userId, idempotencyKey))
                        .flatMap(rec -> projectRepository.findById(rec.getResourceId()))
                        .orElseThrow(() -> CommonErrors.notFound("Project"));
            }
            throw CommonErrors.conflict("Concurrent conflict during project creation");
        }
    }
}
