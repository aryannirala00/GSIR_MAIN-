package com.gsir.knowledge.controller;

import com.gsir.common.api.ApiResponse;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.knowledge.adapter.OntologyRepository;
import com.gsir.knowledge.model.OntologyEntity;
import com.gsir.projects.ProjectAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/ontologies")
public class OntologyController {

    private final OntologyRepository ontologyRepository;
    private final ProjectAccessService projectAccessService;
    private final UuidV7Generator idGenerator;

    public OntologyController(OntologyRepository ontologyRepository, ProjectAccessService projectAccessService, UuidV7Generator idGenerator) {
        this.ontologyRepository = ontologyRepository;
        this.projectAccessService = projectAccessService;
        this.idGenerator = idGenerator;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OntologyEntity>> registerOntology(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody RegisterOntologyRequest request) {
        projectAccessService.requireAccess(projectId, userId);
        
        OntologyEntity entity = new OntologyEntity(
                idGenerator.generate(),
                projectId,
                request.name(),
                request.namespace(),
                request.version(),
                request.domain(),
                request.sourceUri()
        );
        return ResponseEntity.ok(ApiResponse.ok(ontologyRepository.save(entity)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OntologyEntity>>> listOntologies(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId) {
        projectAccessService.requireAccess(projectId, userId);
        return ResponseEntity.ok(ApiResponse.ok(ontologyRepository.findByProjectId(projectId)));
    }
}

record RegisterOntologyRequest(
    String name,
    String namespace,
    String version,
    String domain,
    String sourceUri
) {}
