package com.gsir.assurance.service;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.assurance.adapter.ResearchArtifactRepository;
import com.gsir.assurance.adapter.AssuranceAttestationRepository;
import com.gsir.assurance.model.ResearchArtifactEntity;
import com.gsir.assurance.model.AssuranceAttestationEntity;
import com.gsir.governance.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArtifactAssuranceServiceTest {

    private ResearchArtifactRepository artifactRepository;
    private AssuranceAttestationRepository attestationRepository;
    private UuidV7Generator idGenerator;
    private AuditLogService auditLogService;

    private ArtifactAssuranceService service;

    private boolean saved = false;
    private boolean logged = false;

    @BeforeEach
    void setUp() {
        saved = false;
        logged = false;
        artifactRepository = new ResearchArtifactRepository() {
            public ResearchArtifactEntity save(ResearchArtifactEntity entity) { saved = true; return entity; }
            public Optional<ResearchArtifactEntity> findByIdAndProjectId(UUID id, UUID projectId) {
                if (id.toString().equals("00000000-0000-0000-0000-000000000001")) {
                    ResearchArtifactEntity r = new ResearchArtifactEntity(id, projectId, "DATASET", "uri", "hash", "SHA-256");
                    r.setStatus("REGISTERED");
                    return Optional.of(r);
                }
                return Optional.empty();
            }
            // dummy methods for compilation
            public <S extends ResearchArtifactEntity> List<S> saveAll(Iterable<S> entities) { return null; }
            public void flush() {}
            public <S extends ResearchArtifactEntity> S saveAndFlush(S entity) { return null; }
            public <S extends ResearchArtifactEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            public void deleteAllInBatch(Iterable<ResearchArtifactEntity> entities) {}
            public void deleteAllByIdInBatch(Iterable<UUID> ids) {}
            public void deleteAllInBatch() {}
            public ResearchArtifactEntity getOne(UUID id) { return null; }
            public ResearchArtifactEntity getById(UUID id) { return null; }
            public ResearchArtifactEntity getReferenceById(UUID id) { return null; }
            public <S extends ResearchArtifactEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            public <S extends ResearchArtifactEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            public List<ResearchArtifactEntity> findAll() { return null; }
            public List<ResearchArtifactEntity> findAllById(Iterable<UUID> ids) { return null; }
            public List<ResearchArtifactEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
            public org.springframework.data.domain.Page<ResearchArtifactEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
            public Optional<ResearchArtifactEntity> findById(UUID id) { return Optional.empty(); }
            public boolean existsById(UUID id) { return false; }
            public long count() { return 0; }
            public void deleteById(UUID id) {}
            public void delete(ResearchArtifactEntity entity) {}
            public void deleteAllById(Iterable<? extends UUID> ids) {}
            public void deleteAll(Iterable<? extends ResearchArtifactEntity> entities) {}
            public void deleteAll() {}
            public <S extends ResearchArtifactEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            public <S extends ResearchArtifactEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            public <S extends ResearchArtifactEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            public <S extends ResearchArtifactEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            public <S extends ResearchArtifactEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            public List<ResearchArtifactEntity> findByProjectId(UUID projectId) { return List.of(); }
        };

        attestationRepository = new AssuranceAttestationRepository() {
            public AssuranceAttestationEntity save(AssuranceAttestationEntity entity) { return entity; }
            public <S extends AssuranceAttestationEntity> List<S> saveAll(Iterable<S> entities) { return null; }
            public void flush() {}
            public <S extends AssuranceAttestationEntity> S saveAndFlush(S entity) { return null; }
            public <S extends AssuranceAttestationEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            public void deleteAllInBatch(Iterable<AssuranceAttestationEntity> entities) {}
            public void deleteAllByIdInBatch(Iterable<UUID> ids) {}
            public void deleteAllInBatch() {}
            public AssuranceAttestationEntity getOne(UUID id) { return null; }
            public AssuranceAttestationEntity getById(UUID id) { return null; }
            public AssuranceAttestationEntity getReferenceById(UUID id) { return null; }
            public <S extends AssuranceAttestationEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            public <S extends AssuranceAttestationEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            public List<AssuranceAttestationEntity> findAll() { return null; }
            public List<AssuranceAttestationEntity> findAllById(Iterable<UUID> ids) { return null; }
            public List<AssuranceAttestationEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
            public org.springframework.data.domain.Page<AssuranceAttestationEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
            public Optional<AssuranceAttestationEntity> findById(UUID id) { return Optional.empty(); }
            public boolean existsById(UUID id) { return false; }
            public long count() { return 0; }
            public void deleteById(UUID id) {}
            public void delete(AssuranceAttestationEntity entity) {}
            public void deleteAllById(Iterable<? extends UUID> ids) {}
            public void deleteAll(Iterable<? extends AssuranceAttestationEntity> entities) {}
            public void deleteAll() {}
            public <S extends AssuranceAttestationEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            public <S extends AssuranceAttestationEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            public <S extends AssuranceAttestationEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            public <S extends AssuranceAttestationEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            public <S extends AssuranceAttestationEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            public List<AssuranceAttestationEntity> findByArtifactId(UUID artifactId) { return List.of(); }
        };

        idGenerator = new UuidV7Generator();

        auditLogService = new AuditLogService(null, idGenerator) {
            @Override
            public void logEvent(UUID projectId, UUID actorId, String action, String resourceType, String resourceId, java.util.Map<String, Object> metadata) {
                logged = true;
            }
        };

        service = new ArtifactAssuranceService(artifactRepository, attestationRepository, idGenerator, auditLogService);
    }

    @Test
    void registerArtifact_setsStatusToRegistered() {
        UUID projectId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        ResearchArtifactEntity artifact = service.registerArtifact(projectId, actorId, "MODEL", "s3://models/1", "testhash", "SHA-256");
        
        assertEquals("REGISTERED", artifact.getStatus());
        assertTrue(saved);
        assertTrue(logged);
    }

    @Test
    void verifyArtifactHash_success_setsVerified() {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID actorId = UUID.randomUUID();

        boolean isValid = service.verifyArtifactHash(projectId, artifactId, actorId, "hash");

        assertTrue(isValid);
        assertTrue(saved);
        assertTrue(logged);
    }

    @Test
    void verifyArtifactHash_failure_setsCompromised() {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID actorId = UUID.randomUUID();

        boolean isValid = service.verifyArtifactHash(projectId, artifactId, actorId, "wrong");

        assertFalse(isValid);
        assertTrue(saved);
        assertTrue(logged);
    }

    @Test
    void verifyArtifactHash_wrongProject_throwsException() {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID(); // Does not match 00000000-0000-0000-0000-000000000001
        UUID actorId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> {
            service.verifyArtifactHash(projectId, artifactId, actorId, "hash");
        });
        assertFalse(saved);
    }
    
    @Test
    void attestAssurance_invalidScore_throwsException() {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID actorId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> {
            service.attestAssurance(projectId, artifactId, actorId, "LEVEL1", 1.5, Map.of());
        });
    }
}
