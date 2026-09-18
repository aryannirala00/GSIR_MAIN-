package com.gsir.governance.service;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.governance.adapter.AuditLogRepository;
import com.gsir.governance.model.AuditLogEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UuidV7Generator idGenerator;

    public AuditLogService(AuditLogRepository auditLogRepository, UuidV7Generator idGenerator) {
        this.auditLogRepository = auditLogRepository;
        this.idGenerator = idGenerator;
    }

    public void logEvent(UUID projectId, UUID actorId, String action, String resourceType, String resourceId, Map<String, Object> metadata) {
        AuditLogEntity log = new AuditLogEntity(
                idGenerator.generate(),
                projectId,
                actorId,
                action,
                resourceType,
                resourceId,
                metadata
        );
        auditLogRepository.save(log);
    }

    public List<AuditLogEntity> getProjectAuditLogs(UUID projectId) {
        return auditLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }
}
