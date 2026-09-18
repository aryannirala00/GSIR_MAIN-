package com.gsir.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@ActiveProfiles("test")
class SecurityAuditTest {

    @MockBean
    private AuditEventRepository auditEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditEventRepository, objectMapper, new com.gsir.common.id.UuidV7Generator());
    }

    @Test
    void testAuditRedactsCredentials() throws Exception {
        UUID userId = UUID.randomUUID();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action_detail", "Login attempt");
        metadata.put("password", "super_secret_password");
        metadata.put("apiKey", "sk-12345");
        
        doAnswer(invocation -> {
            AuditEventEntity entity = invocation.getArgument(0);
            assertThat(entity.getMetadata()).doesNotContain("super_secret_password");
            assertThat(entity.getMetadata()).doesNotContain("sk-12345");
            assertThat(entity.getMetadata()).contains("action_detail");
            return entity;
        }).when(auditEventRepository).save(any());

        auditService.logSecurityEvent(userId, null, "LOGIN", "USER", userId, metadata);
        
        verify(auditEventRepository).save(any(AuditEventEntity.class));
    }
}
