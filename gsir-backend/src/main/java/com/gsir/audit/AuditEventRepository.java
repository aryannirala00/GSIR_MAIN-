package com.gsir.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {
    List<AuditEventEntity> findByResourceTypeAndResourceId(String resourceType, UUID resourceId);
    List<AuditEventEntity> findByUserId(UUID userId);
}
