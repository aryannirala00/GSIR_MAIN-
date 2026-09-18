package com.gsir.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.id.UuidV7Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    
    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    private final UuidV7Generator idGenerator;

    public AuditService(AuditEventRepository auditEventRepository, ObjectMapper objectMapper, UuidV7Generator idGenerator) {
        this.idGenerator = idGenerator;
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSecurityEvent(UUID userId, UUID orgId, String action, String resourceType, UUID resourceId, Map<String, Object> metadataMap) {
        try {
            // REDACTION: Ensure no raw passwords or tokens in metadata
            if (metadataMap != null) {
                metadataMap = new java.util.HashMap<>(metadataMap);
                metadataMap.remove("password");
                metadataMap.remove("token");
                metadataMap.remove("secret");
                metadataMap.remove("apiKey");
                metadataMap.remove("credential");
            }
            
            String metadataJson = metadataMap != null ? objectMapper.writeValueAsString(metadataMap) : null;
            
            AuditEventEntity event = new AuditEventEntity(
                idGenerator.generate(),
                userId,
                orgId,
                action,
                resourceType,
                resourceId,
                metadataJson
            );
            
            auditEventRepository.save(event);
            log.info("Security audit event recorded: action={}, user={}, resourceType={}, resourceId={}", 
                     action, userId, resourceType, resourceId);
                     
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit metadata for action: {}", action, e);
        }
    }
}
