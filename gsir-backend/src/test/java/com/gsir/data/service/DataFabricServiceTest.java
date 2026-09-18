package com.gsir.data.service;

import com.gsir.common.error.GsirException;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.data.dto.RegisterDataRequest;
import com.gsir.data.model.DataObjectEntity;
import com.gsir.data.repository.DataObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataFabricServiceTest {

    private DataObjectRepository repository;
    private UuidV7Generator uuidGenerator;
    private DataFabricService service;
    private UUID generatedId;
    private Map<UUID, DataObjectEntity> store = new HashMap<>();

    @BeforeEach
    void setUp() {
        generatedId = UUID.randomUUID();
        
        uuidGenerator = new UuidV7Generator() {
            @Override
            public UUID generate() {
                return generatedId;
            }
        };
        
        repository = new DataObjectRepository() {
            @Override
            public <S extends DataObjectEntity> S save(S entity) {
                store.put(entity.getId(), entity);
                return entity;
            }

            @Override
            public Optional<DataObjectEntity> findById(UUID id) {
                return Optional.ofNullable(store.get(id));
            }

            @Override
            public Page<DataObjectEntity> findByProjectId(UUID projectId, org.springframework.data.domain.Pageable pageable) {
                List<DataObjectEntity> res = store.values().stream()
                    .filter(e -> e.getProjectId().equals(projectId))
                    .toList();
                return new PageImpl<>(res);
            }
            
            @Override
            public List<DataObjectEntity> findByProjectIdAndClassification(UUID projectId, String classification) {
                return store.values().stream()
                    .filter(e -> e.getProjectId().equals(projectId) && classification.equals(e.getClassification()))
                    .toList();
            }
            
            @Override
            public void delete(DataObjectEntity entity) {}

            @Override
            public void deleteById(UUID id) {}
            
            @Override
            public void deleteAll() {}
            
            @Override
            public void deleteAll(Iterable<? extends DataObjectEntity> entities) {}
            
            @Override
            public void deleteAllById(Iterable<? extends UUID> ids) {}
            
            @Override
            public boolean existsById(UUID id) { return store.containsKey(id); }
            
            @Override
            public List<DataObjectEntity> findAll() { return List.copyOf(store.values()); }
            
            @Override
            public List<DataObjectEntity> findAllById(Iterable<UUID> ids) { return List.of(); }
            
            @Override
            public long count() { return store.size(); }
            
            @Override
            public <S extends DataObjectEntity> List<S> saveAll(Iterable<S> entities) { return List.of(); }
            
            @Override
            public void flush() {}
            
            @Override
            public <S extends DataObjectEntity> S saveAndFlush(S entity) { return save(entity); }
            
            @Override
            public <S extends DataObjectEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
            
            @Override
            public void deleteAllInBatch(Iterable<DataObjectEntity> entities) {}
            
            @Override
            public void deleteAllByIdInBatch(Iterable<UUID> ids) {}
            
            @Override
            public void deleteAllInBatch() {}
            
            @Override
            public DataObjectEntity getOne(UUID id) { return store.get(id); }
            
            @Override
            public DataObjectEntity getById(UUID id) { return store.get(id); }
            
            @Override
            public DataObjectEntity getReferenceById(UUID id) { return store.get(id); }
            
            @Override
            public <S extends DataObjectEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return List.of(); }
            
            @Override
            public <S extends DataObjectEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return List.of(); }
            
            @Override
            public List<DataObjectEntity> findAll(org.springframework.data.domain.Sort sort) { return List.of(); }
            
            @Override
            public Page<DataObjectEntity> findAll(org.springframework.data.domain.Pageable pageable) { return new PageImpl<>(List.copyOf(store.values())); }
            
            @Override
            public <S extends DataObjectEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
            
            @Override
            public <S extends DataObjectEntity> Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return Page.empty(); }
            
            @Override
            public <S extends DataObjectEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            
            @Override
            public <S extends DataObjectEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            
            @Override
            public <S extends DataObjectEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        };
        
        service = new DataFabricService(repository, uuidGenerator);
    }

    @Test
    void registerData_Success() {
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        RegisterDataRequest request = new RegisterDataRequest();
        request.setName("Dataset 1");
        request.setDataType("TABULAR");
        request.setClassification("INTERNAL");

        DataObjectEntity result = service.registerData(projectId, ownerId, request);

        assertNotNull(result);
        assertEquals(generatedId, result.getId());
        assertEquals(projectId, result.getProjectId());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals("Dataset 1", result.getName());
        assertEquals("v1.0", result.getVersion());
        assertEquals("INTERNAL", result.getClassification());
        assertEquals("REGISTERED", result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void registerData_MissingRequiredFields_ThrowsException() {
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        RegisterDataRequest request = new RegisterDataRequest();
        request.setName("Dataset 1"); // missing dataType and classification

        GsirException ex = assertThrows(GsirException.class, () -> 
            service.registerData(projectId, ownerId, request)
        );
        assertTrue(ex.getMessage().contains("required"));
    }

    @Test
    void getData_Success() {
        UUID projectId = UUID.randomUUID();
        UUID dataId = UUID.randomUUID();
        
        DataObjectEntity entity = new DataObjectEntity();
        entity.setId(dataId);
        entity.setProjectId(projectId);
        
        store.put(dataId, entity);
        
        DataObjectEntity result = service.getData(projectId, dataId);
        assertEquals(entity, result);
    }

    @Test
    void getData_WrongProject_ThrowsException() {
        UUID projectId = UUID.randomUUID();
        UUID wrongProjectId = UUID.randomUUID();
        UUID dataId = UUID.randomUUID();
        
        DataObjectEntity entity = new DataObjectEntity();
        entity.setId(dataId);
        entity.setProjectId(wrongProjectId);
        
        store.put(dataId, entity);
        
        assertThrows(GsirException.class, () -> service.getData(projectId, dataId));
    }

    @Test
    void classifyData_Success() {
        UUID projectId = UUID.randomUUID();
        UUID dataId = UUID.randomUUID();
        
        DataObjectEntity entity = new DataObjectEntity();
        entity.setId(dataId);
        entity.setProjectId(projectId);
        entity.setClassification("INTERNAL");
        
        store.put(dataId, entity);
        
        DataObjectEntity result = service.classifyData(projectId, dataId, "CONFIDENTIAL");
        
        assertEquals("CONFIDENTIAL", result.getClassification());
        assertEquals("CONFIDENTIAL", store.get(dataId).getClassification());
    }

    @Test
    void listData_Success() {
        UUID projectId = UUID.randomUUID();
        
        DataObjectEntity e1 = new DataObjectEntity();
        e1.setProjectId(projectId);
        e1.setId(UUID.randomUUID());
        store.put(e1.getId(), e1);
        
        Page<DataObjectEntity> result = service.listData(projectId, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }
}
