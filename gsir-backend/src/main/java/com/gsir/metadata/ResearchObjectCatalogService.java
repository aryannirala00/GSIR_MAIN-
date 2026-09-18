package com.gsir.metadata;

import com.gsir.common.error.CommonErrors;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.projects.ProjectAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ResearchObjectCatalogService {

    private final ResearchObjectRepository researchObjectRepository;
    private final MetadataSchemaRepository metadataSchemaRepository;
    private final ProjectAccessService projectAccessService;
    private final UuidV7Generator idGenerator;

    public ResearchObjectCatalogService(
            ResearchObjectRepository researchObjectRepository,
            MetadataSchemaRepository metadataSchemaRepository,
            ProjectAccessService projectAccessService,
            UuidV7Generator idGenerator) {
        this.researchObjectRepository = researchObjectRepository;
        this.metadataSchemaRepository = metadataSchemaRepository;
        this.projectAccessService = projectAccessService;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public ResearchObjectEntity createMetadata(UUID projectId, UUID userId, CreateMetadataRequest request) {
        projectAccessService.requireAccess(projectId, userId);
        
        if (request.schemaId() != null) {
            // Very simple schema format: "name:version"
            String[] parts = request.schemaId().split(":");
            if (parts.length == 2) {
                metadataSchemaRepository.findBySchemaNameAndSchemaVersion(parts[0], parts[1])
                        .orElseThrow(() -> CommonErrors.invalid("Metadata schema not found"));
            }
        }
        
        ResearchObjectEntity entity = new ResearchObjectEntity(
                idGenerator.generate(),
                projectId,
                request.objectType(),
                request.persistentIdentifier(),
                request.versionId() != null ? request.versionId() : "1.0.0",
                request.metadata(),
                request.schemaId()
        );
        return researchObjectRepository.save(entity);
    }
    
    @Transactional(readOnly = true)
    public ResearchObjectEntity getMetadata(UUID projectId, UUID objectId, UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        return researchObjectRepository.findByIdAndProjectId(objectId, projectId)
                .orElseThrow(() -> CommonErrors.notFound("Research object metadata not found"));
    }

    @Transactional
    public ResearchObjectEntity updateMetadata(UUID projectId, UUID objectId, UUID userId, String updatedMetadata, String newVersionId) {
        projectAccessService.requireAccess(projectId, userId);
        ResearchObjectEntity entity = researchObjectRepository.findByIdAndProjectId(objectId, projectId)
                .orElseThrow(() -> CommonErrors.notFound("Research object metadata not found"));
                
        entity.setMetadata(updatedMetadata);
        if (newVersionId != null) {
            entity.setVersionId(newVersionId);
        }
        return researchObjectRepository.save(entity);
    }

    @Transactional
    public MetadataSchemaEntity registerSchema(String schemaName, String schemaVersion, String schemaDefinition) {
        return metadataSchemaRepository.findBySchemaNameAndSchemaVersion(schemaName, schemaVersion)
                .orElseGet(() -> metadataSchemaRepository.save(
                        new MetadataSchemaEntity(idGenerator.generate(), schemaName, schemaVersion, schemaDefinition)
                ));
    }
    
    @Transactional(readOnly = true)
    public List<ResearchObjectEntity> listProjectObjects(UUID projectId, UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        return researchObjectRepository.findByProjectId(projectId);
    }
}
