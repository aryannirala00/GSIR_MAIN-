package com.gsir.knowledge.service;

import com.gsir.knowledge.model.KnowledgeEntity;
import com.gsir.projects.ProjectAccessService;
import com.gsir.projects.ProjectEntity;
import com.gsir.common.error.GsirException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
public class KnowledgeServiceIntegrationTest {

    @Autowired
    private KnowledgeService knowledgeService;

    @TestConfiguration
    static class FakeConfig {
        @Bean
        @Primary
        public ProjectAccessService fakeProjectAccessService() {
            return new ProjectAccessService(null) {
                @Override
                public ProjectEntity requireAccess(UUID projectId, UUID userId) {
                    throw new GsirException("FORBIDDEN", "You do not have permission", org.springframework.http.HttpStatus.FORBIDDEN);
                }
            };
        }
    }

    @Test
    void testCreateEntity_Unauthorized() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        assertThrows(GsirException.class, () -> {
            knowledgeService.createEntity("CONCEPT", "Test", Collections.emptyList(), Collections.emptyMap(), "GENERAL", projectId, userId);
        });
    }

    @Test
    void testSearchKnowledge_Unauthorized() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        assertThrows(GsirException.class, () -> {
            knowledgeService.searchKnowledge("query", projectId, userId);
        });
    }
}
