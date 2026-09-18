package com.gsir.execution.service;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.execution.adapter.ExecutionCostRepository;
import com.gsir.execution.adapter.HumanReviewTaskRepository;
import com.gsir.execution.adapter.ProjectSecretRepository;
import com.gsir.execution.model.ExecutionCostEntity;
import com.gsir.execution.model.HumanReviewTaskEntity;
import com.gsir.execution.model.ProjectSecretEntity;
import com.gsir.security.EncryptionService;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutionServicesTest {

    @Test
    void secretManagementService_storeAndResolve() {
        UuidV7Generator generator = new UuidV7Generator();
        EncryptionService fakeEncService = new EncryptionService("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=") {
            @Override public String encrypt(String raw) { return "ENC-" + raw; }
            @Override public String decrypt(String encrypted) { return encrypted.substring(4); }
        };

        ProjectSecretRepository fakeRepo = new ProjectSecretRepository() {
            private ProjectSecretEntity entity;
            public ProjectSecretEntity save(ProjectSecretEntity e) { this.entity = e; return e; }
            public Optional<ProjectSecretEntity> findByProjectIdAndSecretName(UUID p, String n) {
                return entity != null ? Optional.of(entity) : Optional.empty();
            }
            public List<ProjectSecretEntity> findByProjectId(UUID p) { return List.of(); }
            // dummies
            public <S extends ProjectSecretEntity> List<S> saveAll(Iterable<S> entities) { return null; }
            public void flush() {}
            public <S extends ProjectSecretEntity> S saveAndFlush(S entity) { return null; }
            public <S extends ProjectSecretEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            public void deleteAllInBatch(Iterable<ProjectSecretEntity> entities) {}
            public void deleteAllByIdInBatch(Iterable<UUID> ids) {}
            public void deleteAllInBatch() {}
            public ProjectSecretEntity getOne(UUID id) { return null; }
            public ProjectSecretEntity getById(UUID id) { return null; }
            public ProjectSecretEntity getReferenceById(UUID id) { return null; }
            public <S extends ProjectSecretEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            public <S extends ProjectSecretEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            public List<ProjectSecretEntity> findAll() { return null; }
            public List<ProjectSecretEntity> findAllById(Iterable<UUID> ids) { return null; }
            public List<ProjectSecretEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
            public org.springframework.data.domain.Page<ProjectSecretEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
            public Optional<ProjectSecretEntity> findById(UUID id) { return Optional.empty(); }
            public boolean existsById(UUID id) { return false; }
            public long count() { return 0; }
            public void deleteById(UUID id) {}
            public void delete(ProjectSecretEntity entity) {}
            public void deleteAllById(Iterable<? extends UUID> ids) {}
            public void deleteAll(Iterable<? extends ProjectSecretEntity> entities) {}
            public void deleteAll() {}
            public <S extends ProjectSecretEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            public <S extends ProjectSecretEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            public <S extends ProjectSecretEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            public <S extends ProjectSecretEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            public <S extends ProjectSecretEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        };

        SecretManagementService service = new SecretManagementService(fakeRepo, fakeEncService, generator);

        UUID projectId = UUID.randomUUID();
        service.storeSecret(projectId, "API_KEY", "my-secret");

        String resolved = service.resolveSecret(projectId, "API_KEY");
        assertEquals("my-secret", resolved);
    }

    @Test
    void costTrackingService_recordCost() {
        UuidV7Generator generator = new UuidV7Generator();
        ExecutionCostRepository fakeRepo = new ExecutionCostRepository() {
            public ExecutionCostEntity save(ExecutionCostEntity e) { return e; }
            public List<ExecutionCostEntity> findByProjectId(UUID p) { return List.of(); }
            public List<ExecutionCostEntity> findByWorkflowId(UUID p) { return List.of(); }
            // dummies
            public <S extends ExecutionCostEntity> List<S> saveAll(Iterable<S> entities) { return null; }
            public void flush() {}
            public <S extends ExecutionCostEntity> S saveAndFlush(S entity) { return null; }
            public <S extends ExecutionCostEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            public void deleteAllInBatch(Iterable<ExecutionCostEntity> entities) {}
            public void deleteAllByIdInBatch(Iterable<UUID> ids) {}
            public void deleteAllInBatch() {}
            public ExecutionCostEntity getOne(UUID id) { return null; }
            public ExecutionCostEntity getById(UUID id) { return null; }
            public ExecutionCostEntity getReferenceById(UUID id) { return null; }
            public <S extends ExecutionCostEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            public <S extends ExecutionCostEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            public List<ExecutionCostEntity> findAll() { return null; }
            public List<ExecutionCostEntity> findAllById(Iterable<UUID> ids) { return null; }
            public List<ExecutionCostEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
            public org.springframework.data.domain.Page<ExecutionCostEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
            public Optional<ExecutionCostEntity> findById(UUID id) { return Optional.empty(); }
            public boolean existsById(UUID id) { return false; }
            public long count() { return 0; }
            public void deleteById(UUID id) {}
            public void delete(ExecutionCostEntity entity) {}
            public void deleteAllById(Iterable<? extends UUID> ids) {}
            public void deleteAll(Iterable<? extends ExecutionCostEntity> entities) {}
            public void deleteAll() {}
            public <S extends ExecutionCostEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            public <S extends ExecutionCostEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            public <S extends ExecutionCostEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            public <S extends ExecutionCostEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            public <S extends ExecutionCostEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        };
        
        com.gsir.projects.ProjectRepository fakeProjRepo = new com.gsir.projects.ProjectRepository() {
            public Optional<com.gsir.projects.ProjectEntity> findById(UUID id) { return Optional.empty(); }
            public List<com.gsir.projects.ProjectEntity> findByWorkspaceId(UUID wid) { return List.of(); }
            public List<com.gsir.projects.ProjectEntity> findAccessibleProjects(UUID orgId, UUID userId) { return List.of(); }
            public Optional<com.gsir.projects.ProjectEntity> findAccessibleProject(UUID p1, UUID p2) { return Optional.empty(); }
            public Optional<com.gsir.projects.ProjectEntity> findAccessibleProject(UUID oId, UUID wId, UUID pId, UUID uId) { return Optional.empty(); }
            public Optional<com.gsir.projects.ProjectEntity> findByIdForUpdate(UUID id) { return Optional.empty(); }
            public <S extends com.gsir.projects.ProjectEntity> S save(S entity) { return null; }
            public <S extends com.gsir.projects.ProjectEntity> List<S> saveAll(Iterable<S> entities) { return null; }
            public void flush() {}
            public <S extends com.gsir.projects.ProjectEntity> S saveAndFlush(S entity) { return null; }
            public <S extends com.gsir.projects.ProjectEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            public void deleteAllInBatch(Iterable<com.gsir.projects.ProjectEntity> entities) {}
            public void deleteAllByIdInBatch(Iterable<UUID> ids) {}
            public void deleteAllInBatch() {}
            public com.gsir.projects.ProjectEntity getOne(UUID id) { return null; }
            public com.gsir.projects.ProjectEntity getById(UUID id) { return null; }
            public com.gsir.projects.ProjectEntity getReferenceById(UUID id) { return null; }
            public <S extends com.gsir.projects.ProjectEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            public <S extends com.gsir.projects.ProjectEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            public List<com.gsir.projects.ProjectEntity> findAll() { return null; }
            public List<com.gsir.projects.ProjectEntity> findAllById(Iterable<UUID> ids) { return null; }
            public List<com.gsir.projects.ProjectEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
            public org.springframework.data.domain.Page<com.gsir.projects.ProjectEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
            public boolean existsById(UUID id) { return false; }
            public long count() { return 0; }
            public void deleteById(UUID id) {}
            public void delete(com.gsir.projects.ProjectEntity entity) {}
            public void deleteAllById(Iterable<? extends UUID> ids) {}
            public void deleteAll(Iterable<? extends com.gsir.projects.ProjectEntity> entities) {}
            public void deleteAll() {}
            public <S extends com.gsir.projects.ProjectEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            public <S extends com.gsir.projects.ProjectEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            public <S extends com.gsir.projects.ProjectEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            public <S extends com.gsir.projects.ProjectEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            public <S extends com.gsir.projects.ProjectEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        };

        CostTrackingService service = new CostTrackingService(fakeRepo, fakeProjRepo, generator);

        UUID projectId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();

        ExecutionCostEntity cost = service.recordCost(projectId, workflowId, stepId, 0.5, 1.2, 0.3, "USD");
        assertEquals(2.0, cost.getTotalCost());
        assertEquals("USD", cost.getCurrency());
    }

    @Test
    void humanReviewService_createAndResolve() {
        UuidV7Generator generator = new UuidV7Generator();
        HumanReviewTaskRepository fakeRepo = new HumanReviewTaskRepository() {
            private HumanReviewTaskEntity entity;
            public HumanReviewTaskEntity save(HumanReviewTaskEntity e) { this.entity = e; return e; }
            public Optional<HumanReviewTaskEntity> findByIdAndProjectId(UUID id, UUID p) {
                return entity != null ? Optional.of(entity) : Optional.empty();
            }
            public List<HumanReviewTaskEntity> findByProjectId(UUID p) { return List.of(); }
            public List<HumanReviewTaskEntity> findByWorkflowId(UUID p) { return List.of(); }
            // dummies
            public <S extends HumanReviewTaskEntity> List<S> saveAll(Iterable<S> entities) { return null; }
            public void flush() {}
            public <S extends HumanReviewTaskEntity> S saveAndFlush(S entity) { return null; }
            public <S extends HumanReviewTaskEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            public void deleteAllInBatch(Iterable<HumanReviewTaskEntity> entities) {}
            public void deleteAllByIdInBatch(Iterable<UUID> ids) {}
            public void deleteAllInBatch() {}
            public HumanReviewTaskEntity getOne(UUID id) { return null; }
            public HumanReviewTaskEntity getById(UUID id) { return null; }
            public HumanReviewTaskEntity getReferenceById(UUID id) { return null; }
            public <S extends HumanReviewTaskEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            public <S extends HumanReviewTaskEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            public List<HumanReviewTaskEntity> findAll() { return null; }
            public List<HumanReviewTaskEntity> findAllById(Iterable<UUID> ids) { return null; }
            public List<HumanReviewTaskEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
            public org.springframework.data.domain.Page<HumanReviewTaskEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
            public Optional<HumanReviewTaskEntity> findById(UUID id) { return Optional.empty(); }
            public boolean existsById(UUID id) { return false; }
            public long count() { return 0; }
            public void deleteById(UUID id) {}
            public void delete(HumanReviewTaskEntity entity) {}
            public void deleteAllById(Iterable<? extends UUID> ids) {}
            public void deleteAll(Iterable<? extends HumanReviewTaskEntity> entities) {}
            public void deleteAll() {}
            public <S extends HumanReviewTaskEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            public <S extends HumanReviewTaskEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            public <S extends HumanReviewTaskEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            public <S extends HumanReviewTaskEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            public <S extends HumanReviewTaskEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        };

        com.gsir.jobs.JobService fakeJobService = new com.gsir.jobs.JobService(null, null, null, null, null, null) {
            @Override
            public com.gsir.jobs.JobEntity submitJob(UUID u, String i, String t, String p) { return null; }
        };

        HumanReviewService service = new HumanReviewService(fakeRepo, fakeJobService, generator);

        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        HumanReviewTaskEntity entity = service.createReviewTask(projectId, UUID.randomUUID(), UUID.randomUUID(), Map.of());
        assertEquals("PENDING", entity.getStatus());

        HumanReviewTaskEntity resolved = service.resolveTask(projectId, entity.getId(), reviewerId, Map.of("approved", true));
        assertEquals("RESOLVED", resolved.getStatus());
        assertEquals(reviewerId, resolved.getReviewerId());
        assertTrue((Boolean) resolved.getDecision().get("approved"));
    }
}





