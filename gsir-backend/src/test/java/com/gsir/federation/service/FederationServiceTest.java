package com.gsir.federation.service;

import com.gsir.common.error.GsirException;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.federation.dto.RegisterSiteRequest;
import com.gsir.federation.dto.SubmitRemoteExecutionRequest;
import com.gsir.federation.model.FederationSiteEntity;
import com.gsir.federation.model.RemoteExecutionEntity;
import com.gsir.federation.repository.FederationSiteRepository;
import com.gsir.federation.repository.RemoteExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FederationServiceTest {

    private FederationSiteRepository siteRepository;
    private RemoteExecutionRepository executionRepository;
    private UuidV7Generator uuidGenerator;
    private FederationService service;
    private UUID generatedId;
    
    private Map<UUID, FederationSiteEntity> siteStore = new HashMap<>();
    private Map<UUID, RemoteExecutionEntity> execStore = new HashMap<>();

    @BeforeEach
    void setUp() {
        generatedId = UUID.randomUUID();
        
        uuidGenerator = new UuidV7Generator() {
            @Override
            public UUID generate() {
                return generatedId;
            }
        };
        
        siteRepository = mock(FederationSiteRepository.class);
        executionRepository = mock(RemoteExecutionRepository.class);
        
        com.gsir.projects.ProjectAccessService projectAccessService = new com.gsir.projects.ProjectAccessService(null) {
            @Override
            public com.gsir.projects.ProjectEntity requireAccess(UUID projectId, UUID userId) {
                return null;
            }
        };
        
        service = new FederationService(siteRepository, executionRepository, uuidGenerator, projectAccessService);
    }

    @Test
    void registerSite_Success() {
        RegisterSiteRequest req = new RegisterSiteRequest();
        req.setName("Site Alpha");
        req.setApiUrl("https://alpha.site.com");

        when(siteRepository.findByName("Site Alpha")).thenReturn(Optional.empty());
        when(siteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FederationSiteEntity site = service.registerSite(req);

        assertNotNull(site);
        assertEquals(generatedId, site.getId());
        assertEquals("Site Alpha", site.getName());
        assertEquals("ACTIVE", site.getStatus());
        
        verify(siteRepository).save(any());
    }

    @Test
    void registerSite_Duplicate_ThrowsException() {
        RegisterSiteRequest req = new RegisterSiteRequest();
        req.setName("Site Alpha");
        req.setApiUrl("https://alpha.site.com");

        when(siteRepository.findByName("Site Alpha")).thenReturn(Optional.of(new FederationSiteEntity()));

        assertThrows(GsirException.class, () -> service.registerSite(req));
    }

    @Test
    void submitRemoteExecution_Success() {
        UUID siteId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        FederationSiteEntity site = new FederationSiteEntity();
        site.setId(siteId);
        
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(site));
        when(executionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubmitRemoteExecutionRequest req = new SubmitRemoteExecutionRequest();
        req.setWorkflowId(UUID.randomUUID());
        req.setPayload("{\"key\":\"val\"}");

        RemoteExecutionEntity execution = service.submitRemoteExecution(projectId, userId, siteId, req);

        assertNotNull(execution);
        assertEquals(generatedId, execution.getId());
        assertEquals(siteId, execution.getTargetSiteId());
        assertEquals("SUBMITTED", execution.getStatus());
    }
}
