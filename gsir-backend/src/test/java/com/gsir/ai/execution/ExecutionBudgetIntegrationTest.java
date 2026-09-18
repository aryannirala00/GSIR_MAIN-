package com.gsir.ai.execution;

import com.gsir.projects.ProjectEntity;
import com.gsir.projects.ProjectRepository;
import com.gsir.organizations.OrganizationEntity;
import com.gsir.organizations.OrganizationRepository;
import com.gsir.workspaces.WorkspaceEntity;
import com.gsir.workspaces.WorkspaceRepository;
import com.gsir.users.UserEntity;
import com.gsir.users.UserRepository;
import com.gsir.common.id.UuidV7Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ExecutionBudgetIntegrationTest {

    @Autowired private ExecutionBudgetService budgetService;
    @Autowired private ExecutionBudgetRepository repository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private WorkspaceRepository workspaceRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private UserRepository userRepository;
    
    private final UuidV7Generator idGenerator = new UuidV7Generator();
    private UUID projectId;

    @BeforeEach
    void setUp() {
        UserEntity user = userRepository.save(new UserEntity(idGenerator.generate(), "user" + System.nanoTime() + "@example.com", "hash", "ACTIVE"));
        OrganizationEntity org = organizationRepository.save(new OrganizationEntity(idGenerator.generate(), "Test Org", user.getId()));
        WorkspaceEntity workspace = workspaceRepository.save(new WorkspaceEntity(idGenerator.generate(), org.getId(), "Test Workspace"));
        ProjectEntity project = projectRepository.save(new ProjectEntity(idGenerator.generate(), workspace.getId(), "Test Project", "Description"));
        projectId = project.getId();
    }

    @Test
    public void testBudgetAdmissionAndConsumption() {
        UUID executionId = UUID.randomUUID();
        
        ExecutionBudget budget = new ExecutionBudget(5, 5, 5, 10000, 100, 1.0);
        budgetService.createBudget(executionId, projectId, budget);
        
        // Consume steps
        budgetService.consumeStep(executionId);
        budgetService.consumeStep(executionId);
        
        ExecutionBudgetEntity entity = repository.findById(executionId).orElseThrow();
        assertEquals(2, entity.getConsumedSteps());
        
        // Consume to exhaust
        budgetService.consumeStep(executionId);
        budgetService.consumeStep(executionId);
        budgetService.consumeStep(executionId);
        
        // Exceed budget
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> budgetService.consumeStep(executionId));
        assertTrue(ex.getMessage().contains("exhausted"));
    }

    @Test
    public void testConcurrentBudgetConsumption() throws InterruptedException {
        UUID executionId = UUID.randomUUID();
        
        ExecutionBudget budget = new ExecutionBudget(10, 10, 10, 10000, 100, 1.0);
        budgetService.createBudget(executionId, projectId, budget);
        
        int threads = 50;
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threads);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threads);
        
        java.util.concurrent.atomic.AtomicInteger successful = new java.util.concurrent.atomic.AtomicInteger(0);
        
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    budgetService.consumeStep(executionId);
                    successful.incrementAndGet();
                } catch (Exception e) {
                    // Expected when exhausted
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        ExecutionBudgetEntity entity = repository.findById(executionId).orElseThrow();
        assertEquals(10, entity.getConsumedSteps());
        assertEquals(10, successful.get());
    }

    @Test
    public void testRealTokenCostConsumption() {
        UUID executionId = UUID.randomUUID();
        
        ExecutionBudget budget = new ExecutionBudget(10, 10, 10, 10000, 1500, 1.0);
        budgetService.createBudget(executionId, projectId, budget);
        
        // Simulating AiResponse metadata mapping to consumption
        com.gsir.ai.provider.TokenUsage tokenUsage = new com.gsir.ai.provider.TokenUsage(500, 200, 700, true);
        double actualCost = 0.5; // From pricing engine
        
        budgetService.consumeTokens(executionId, tokenUsage.totalTokens());
        budgetService.consumeCost(executionId, actualCost);
        
        ExecutionBudgetEntity entity = repository.findById(executionId).orElseThrow();
        assertEquals(700, entity.getConsumedTokens());
        assertEquals(0.5, entity.getConsumedCost(), 0.001);
        
        // Next request which exceeds budget
        com.gsir.ai.provider.TokenUsage tokenUsage2 = new com.gsir.ai.provider.TokenUsage(800, 100, 900, true);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> budgetService.consumeTokens(executionId, tokenUsage2.totalTokens()));
        assertTrue(ex.getMessage().contains("exhausted"));
    }

    @Test
    public void testRuntimeBudgetExhaustion() {
        // We use a mock clock to simulate time passing exactly without sleeping
        java.time.Clock mockClock = org.mockito.Mockito.mock(java.time.Clock.class);
        org.mockito.Mockito.when(mockClock.millis()).thenReturn(0L).thenReturn(20000L); // 20s later
        
        org.springframework.beans.factory.ObjectProvider<java.time.Clock> clockProvider = org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class);
        org.mockito.Mockito.when(clockProvider.getIfAvailable(org.mockito.ArgumentMatchers.any())).thenReturn(mockClock);
        
        org.springframework.beans.factory.ObjectProvider<com.gsir.provenance.service.ExecutionProvenanceService> provProvider = org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class);
        org.mockito.Mockito.when(provProvider.getIfAvailable()).thenReturn(null);
        
        ExecutionBudgetService customService = new ExecutionBudgetService(repository, clockProvider, provProvider);
        
        UUID executionId = UUID.randomUUID();
        // maxRuntimeMillis = 10000 (10s)
        ExecutionBudget budget = new ExecutionBudget(10, 10, 10, 10000, 1500, 1.0);
        customService.createBudget(executionId, projectId, budget);
        
        // This will now use clock.millis() which returns 20000, and createdAt is at 0.
        // 20s > 10s budget => throws
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> customService.validateGuard(executionId));
        assertTrue(ex.getMessage().contains("budget exceeded"), "Should mention budget exceeded");
    }
}
