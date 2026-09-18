package com.gsir.experiments.service;

import com.gsir.common.error.GsirException;
import com.gsir.experiments.model.ExperimentEntity;
import com.gsir.experiments.model.ExperimentStatus;
import com.gsir.projects.ProjectAccessService;
import com.gsir.projects.ProjectEntity;
import com.gsir.projects.ProjectRepository;
import com.gsir.users.UserEntity;
import com.gsir.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
@ActiveProfiles("test")
public class ExperimentIntegrationTest {

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private UUID userId;
    private UUID projectId;
    private UUID otherProjectId;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ProjectAccessService fakeProjectAccessService(ProjectRepository projectRepository) {
            return new ProjectAccessService(projectRepository) {
                @Override
                public ProjectEntity requireAccess(UUID projectId, UUID userId) {
                    if (projectId.equals(UUID.fromString("00000000-0000-0000-0000-000000000002"))) {
                        throw new GsirException("FORBIDDEN", "Forbidden", org.springframework.http.HttpStatus.FORBIDDEN);
                    }
                    return null; // Return null since we just care about it not throwing
                }
            };
        }

        @Bean
        @Primary
        public com.gsir.provenance.repository.ProvenanceRepository fakeProvenanceRepository() {
            return new com.gsir.provenance.repository.ProvenanceRepository(null, null) {
                @Override
                public com.gsir.provenance.model.ProvActivity saveActivity(com.gsir.provenance.model.ProvActivity activity) {
                    return activity;
                }
            };
        }
    }

    @BeforeEach
    void setUp() {
        experimentRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        
        userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, "test" + userId + "@example.com", "hash", "ACTIVE");
        userRepository.save(user);

        projectId = UUID.randomUUID();
        ProjectEntity project = new ProjectEntity(projectId, UUID.randomUUID(), "Test Project", "Desc");
        projectRepository.save(project);

        otherProjectId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    }

    @Test
    void testExperimentCreationAndPersistence() {
        ExperimentEntity exp = experimentService.createExperiment(projectId, userId, "Test Exp", "Desc", "Q?", null);
        assertNotNull(exp.getId());
        assertEquals(ExperimentStatus.DRAFT, exp.getStatus());

        ExperimentEntity fetched = experimentService.getExperiment(exp.getId(), projectId, userId);
        assertEquals("Test Exp", fetched.getName());
    }

    @Test
    void testTenantIsolation() {
        ExperimentEntity exp = experimentService.createExperiment(projectId, userId, "Test Exp", "Desc", "Q?", null);

        assertThrows(GsirException.class, () -> {
            experimentService.getExperiment(exp.getId(), otherProjectId, userId);
        });
    }

    @Test
    void testLifecycleTransitions() {
        ExperimentEntity exp = experimentService.createExperiment(projectId, userId, "Test Exp", "Desc", "Q?", null);
        
        // DRAFT -> DESIGNED
        exp = experimentService.updateExperimentStatus(exp.getId(), projectId, userId, "DESIGNED");
        assertEquals(ExperimentStatus.DESIGNED, exp.getStatus());

        // DESIGNED -> READY
        exp = experimentService.updateExperimentStatus(exp.getId(), projectId, userId, "READY");
        assertEquals(ExperimentStatus.READY, exp.getStatus());

        // READY -> SCHEDULED
        exp = experimentService.updateExperimentStatus(exp.getId(), projectId, userId, "SCHEDULED");
        assertEquals(ExperimentStatus.SCHEDULED, exp.getStatus());

        // SCHEDULED -> RUNNING
        exp = experimentService.updateExperimentStatus(exp.getId(), projectId, userId, "RUNNING");
        assertEquals(ExperimentStatus.RUNNING, exp.getStatus());

        // RUNNING -> COMPLETED
        exp = experimentService.updateExperimentStatus(exp.getId(), projectId, userId, "COMPLETED");
        assertEquals(ExperimentStatus.COMPLETED, exp.getStatus());
    }

    @Test
    void testInvalidLifecycleTransition() {
        ExperimentEntity exp = experimentService.createExperiment(projectId, userId, "Test Exp", "Desc", "Q?", null);
        
        // DRAFT -> RUNNING is invalid
        GsirException ex = assertThrows(GsirException.class, () -> {
            experimentService.updateExperimentStatus(exp.getId(), projectId, userId, "RUNNING");
        });
        assertEquals("INVALID_TRANSITION", ex.getCode());
    }

    @Test
    void testProtocolAndRunCreation() {
        ExperimentEntity exp = experimentService.createExperiment(projectId, userId, "Test Exp", "Desc", "Q?", null);

        // create protocol
        com.gsir.experiments.model.ExperimentProtocolEntity proto = experimentService.createProtocol(projectId, exp.getId(), userId, "Prep", "Step 1", "Goggles");
        assertNotNull(proto.getId());

        // create run
        com.gsir.experiments.model.ExperimentRunEntity run = experimentService.createRun(projectId, exp.getId(), userId, proto.getId());
        assertNotNull(run.getId());
        assertEquals("CREATED", run.getStatus());

        // create observation
        com.gsir.experiments.model.ExperimentObservationEntity obs = experimentService.recordObservation(projectId, exp.getId(), run.getId(), userId, null, "10", "kg", "sensor_1", null, null, null, null, null);
        assertNotNull(obs.getId());
        assertEquals("10", obs.getValue());

        // create result
        com.gsir.experiments.model.ExperimentResultEntity res = experimentService.recordResult(projectId, exp.getId(), run.getId(), userId, "SUMMARY", "Pass", null, 0.9, null, null, null, null, null);
        assertNotNull(res.getId());
        assertEquals("STORED", res.getValidationStatus());
    }

    @Test
    void testCrossProjectValidation() {
        ExperimentEntity exp = experimentService.createExperiment(projectId, userId, "Test Exp", "Desc", "Q?", null);

        // test security
        GsirException e1 = assertThrows(GsirException.class, () -> {
            experimentService.getExperiment(exp.getId(), otherProjectId, userId);
        });
        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, e1.getStatus());

        GsirException e2 = assertThrows(GsirException.class, () -> {
            // Using a fake runId, it should fail access check before run lookup because otherProject is forbidden
            experimentService.recordObservation(otherProjectId, exp.getId(), UUID.randomUUID(), userId, null, "10", "kg", "sensor_1", null, null, null, null, null);
        });
        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, e2.getStatus());
    }
}
