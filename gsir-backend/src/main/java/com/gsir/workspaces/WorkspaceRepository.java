package com.gsir.workspaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, UUID> {

    /**
     * Verifies user membership in both the Workspace and its parent Organization.
     * Prevents cross-tenant actions on workspaces.
     */
    @Query("""
        SELECT w
        FROM Workspace w
        JOIN WorkspaceMembership wm ON wm.id.workspaceId = w.id
        JOIN OrganizationMembership om ON om.id.organizationId = w.organizationId AND om.id.userId = wm.id.userId
        WHERE w.id = :workspaceId
          AND wm.id.userId = :userId
    """)
    Optional<WorkspaceEntity> findAccessibleWorkspace(
            @Param("workspaceId") UUID workspaceId,
            @Param("userId") UUID userId
    );
}

