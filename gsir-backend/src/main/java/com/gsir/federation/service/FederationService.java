package com.gsir.federation.service;

import com.gsir.common.error.GsirException;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.federation.dto.RegisterSiteRequest;
import com.gsir.federation.dto.SubmitRemoteExecutionRequest;
import com.gsir.federation.model.FederationSiteEntity;
import com.gsir.federation.model.RemoteExecutionEntity;
import com.gsir.federation.repository.FederationSiteRepository;
import com.gsir.federation.repository.RemoteExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FederationService {

    private final FederationSiteRepository siteRepository;
    private final RemoteExecutionRepository executionRepository;
    private final UuidV7Generator uuidGenerator;
    private final com.gsir.projects.ProjectAccessService projectAccessService;

    public FederationService(
            FederationSiteRepository siteRepository,
            RemoteExecutionRepository executionRepository,
            UuidV7Generator uuidGenerator,
            com.gsir.projects.ProjectAccessService projectAccessService) {
        this.siteRepository = siteRepository;
        this.executionRepository = executionRepository;
        this.uuidGenerator = uuidGenerator;
        this.projectAccessService = projectAccessService;
    }

    @Transactional
    public FederationSiteEntity registerSite(RegisterSiteRequest request) {
        if (request.getName() == null || request.getApiUrl() == null) {
            throw com.gsir.common.error.CommonErrors.invalid("Site name and api_url are required");
        }

        siteRepository.findByName(request.getName()).ifPresent(site -> {
            throw com.gsir.common.error.CommonErrors.conflict("Site with this name already exists");
        });

        FederationSiteEntity site = new FederationSiteEntity();
        site.setId(uuidGenerator.generate());
        site.setName(request.getName());
        site.setOrganization(request.getOrganization());
        site.setApiUrl(request.getApiUrl());
        site.setStatus("ACTIVE");
        site.setCapabilities(request.getCapabilities());
        site.setPolicies(request.getPolicies());

        return siteRepository.save(site);
    }

    public List<FederationSiteEntity> listSites() {
        return siteRepository.findAll();
    }

    public FederationSiteEntity getSite(UUID siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> com.gsir.common.error.CommonErrors.notFound("Site not found"));
    }

    @Transactional
    public RemoteExecutionEntity submitRemoteExecution(UUID projectId, UUID userId, UUID siteId, SubmitRemoteExecutionRequest request) {
        if (request.getWorkflowId() == null) {
            throw com.gsir.common.error.CommonErrors.invalid("workflowId is required");
        }
        
        projectAccessService.requireAccess(projectId, userId);

        FederationSiteEntity site = getSite(siteId);

        RemoteExecutionEntity execution = new RemoteExecutionEntity();
        execution.setId(uuidGenerator.generate());
        execution.setWorkflowId(request.getWorkflowId());
        execution.setTargetSiteId(site.getId());
        execution.setStatus("SUBMITTED");
        execution.setPayload(request.getPayload());

        return executionRepository.save(execution);
    }

    public RemoteExecutionEntity getRemoteExecution(UUID projectId, UUID userId, UUID executionId) {
        projectAccessService.requireAccess(projectId, userId);
        return executionRepository.findById(executionId)
                .orElseThrow(() -> com.gsir.common.error.CommonErrors.notFound("Execution not found"));
    }
}
