package com.gsir.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Security-enforced repository for Project persistence (Volume II Part 12).
 *
 * CRITICAL TENANCY INVARIANT:
 * Never query projects by projectId alone for security-sensitive operations.
 * All access must strictly join through the canonical hierarchy:
 * Organization -> Workspace -> Project, ensuring the user has active
 * membership at both the Workspace level and the enclosing Organization level.
 */
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    /**
     * Finds a project by ID only if the given user is an authorized member of both
     * the project's enclosing workspace and that workspace's parent organization.
     */
    @Query("""
        SELECT p
        FROM Project p
        JOIN Workspace w ON w.id = p.workspaceId
        JOIN WorkspaceMembership wm ON wm.id.workspaceId = w.id
        JOIN OrganizationMembership om ON om.id.organizationId = w.organizationId AND om.id.userId = wm.id.userId
        WHERE p.id = :projectId
          AND wm.id.userId = :userId
    """)
    Optional<ProjectEntity> findAccessibleProject(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );

    /**
     * Enforces strict boundary verification when a client explicitly supplies
     * hierarchy identifiers (organizationId and workspaceId).
     */
    @Query("""
        SELECT p
        FROM Project p
        JOIN Workspace w ON w.id = p.workspaceId
        JOIN WorkspaceMembership wm ON wm.id.workspaceId = w.id
        JOIN OrganizationMembership om ON om.id.organizationId = w.organizationId AND om.id.userId = wm.id.userId
        WHERE p.id = :projectId
          AND p.workspaceId = :workspaceId
          AND w.organizationId = :organizationId
          AND wm.id.userId = :userId
    """)
    Optional<ProjectEntity> findAccessibleProject(
            @Param("organizationId") UUID organizationId,
            @Param("workspaceId") UUID workspaceId,
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );

    /**
     * Lists all projects accessible by the user, optionally filtered by workspaceId.
     */
    @Query("""
        SELECT p
        FROM Project p
        JOIN Workspace w ON w.id = p.workspaceId
        JOIN WorkspaceMembership wm ON wm.id.workspaceId = w.id
        JOIN OrganizationMembership om ON om.id.organizationId = w.organizationId AND om.id.userId = wm.id.userId
        WHERE wm.id.userId = :userId
          AND (:workspaceId IS NULL OR p.workspaceId = :workspaceId)
    """)
    List<ProjectEntity> findAccessibleProjects(
            @Param("userId") UUID userId,
            @Param("workspaceId") UUID workspaceId
    );

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<ProjectEntity> findByIdForUpdate(@Param("id") UUID id);
}
