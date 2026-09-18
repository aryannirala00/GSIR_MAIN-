package com.gsir.metadata;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.projects.ProjectAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResearchObjectCatalogServiceTest {

    private ResearchObjectCatalogService catalogService;
    private UUID projectId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        ProjectAccessService projectAccessService = new ProjectAccessService(null) {
            @Override
            public com.gsir.projects.ProjectEntity requireAccess(UUID pId, UUID uId) {
                if (!pId.equals(projectId) || !uId.equals(userId)) {
                    throw new RuntimeException("Denied");
                }
                return null;
            }
        };

        ResearchObjectRepository fakeRepository = new ResearchObjectRepository() {
            private ResearchObjectEntity stored = null;
            
            @Override
            public ResearchObjectEntity save(ResearchObjectEntity entity) {
                stored = entity;
                return entity;
            }
            @Override
            public Optional<ResearchObjectEntity> findByIdAndProjectId(UUID id, UUID pId) {
                if (stored != null && stored.getId().equals(id) && stored.getProjectId().equals(pId)) {
                    return Optional.of(stored);
                }
                return Optional.empty();
            }
            // other methods returning null or empty for brevity
            @Override public java.util.List<ResearchObjectEntity> findAll() { return null; }
            @Override public java.util.List<ResearchObjectEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
            @Override public org.springframework.data.domain.Page<ResearchObjectEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public java.util.List<ResearchObjectEntity> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public long count() { return 0; }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(ResearchObjectEntity entity) {}
            @Override public void deleteAllById(Iterable<? extends UUID> uuids) {}
            @Override public void deleteAll(Iterable<? extends ResearchObjectEntity> entities) {}
            @Override public void deleteAll() {}
            @Override public <S extends ResearchObjectEntity> java.util.List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public Optional<ResearchObjectEntity> findById(UUID uuid) { return Optional.empty(); }
            @Override public boolean existsById(UUID uuid) { return false; }
            @Override public void flush() {}
            @Override public <S extends ResearchObjectEntity> S saveAndFlush(S entity) { return null; }
            @Override public <S extends ResearchObjectEntity> java.util.List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<ResearchObjectEntity> entities) {}
            @Override public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
            @Override public void deleteAllInBatch() {}
            @Override public ResearchObjectEntity getOne(UUID uuid) { return null; }
            @Override public ResearchObjectEntity getById(UUID uuid) { return null; }
            @Override public ResearchObjectEntity getReferenceById(UUID uuid) { return null; }
            @Override public <S extends ResearchObjectEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            @Override public <S extends ResearchObjectEntity> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends ResearchObjectEntity> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends ResearchObjectEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public <S extends ResearchObjectEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends ResearchObjectEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public <S extends ResearchObjectEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
            @Override public java.util.List<ResearchObjectEntity> findByProjectId(UUID projectId) { return null; }
        };

        catalogService = new ResearchObjectCatalogService(fakeRepository, null, projectAccessService, new UuidV7Generator());
    }

    @Test
    void testCreateAndGetMetadata() {
        CreateMetadataRequest req = new CreateMetadataRequest("DATASET", "doi:10.123/456", "1.0.0", "{\"name\":\"Test\"}", null);
        ResearchObjectEntity created = catalogService.createMetadata(projectId, userId, req);
        assertNotNull(created.getId());
        assertEquals("DATASET", created.getObjectType());
        assertEquals("doi:10.123/456", created.getPersistentIdentifier());

        ResearchObjectEntity retrieved = catalogService.getMetadata(projectId, created.getId(), userId);
        assertNotNull(retrieved);
        assertEquals("{\"name\":\"Test\"}", retrieved.getMetadata());
    }
}

