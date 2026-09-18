package com.gsir.projects;

import com.gsir.common.error.CommonErrors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service enforcing multi-tenant containment and access boundaries for Project operations
 * (GSIR Master Coding Document Volume II Part 10 & Part 11).
 *
 * CANONICAL CONTAINMENT HIERARCHY:
 * Organization (top-level tenant)
 *     ↓
 * Workspace (department / lab / research group)
 *     ↓
 * Project (specific investigation / codebase / study)
 *
 * CRITICAL SECURITY INVARIANTS:
 * 1. An unauthorized resource is reported as NOT FOUND (404 / RESOURCE_NOT_FOUND) rather than
 *    FORBIDDEN (403). This avoids existence oracle leaks where an attacker could deduce the
 *    presence of confidential projects in other organizations.
 * 2. Client-supplied organization and workspace identifiers are untrusted input. They are
 *    always reconciled against the actual database relational hierarchy.
 * 3. Authorization is strictly enforced at the data-access service layer, not just the controller.
 */
@Service
@Transactional(readOnly = true)
public class ProjectAccessService {

    private final ProjectRepository projectRepository;

    public ProjectAccessService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Authorizes and returns the requested project if the user possesses valid membership
     * in the project's enclosing workspace and parent organization.
     *
     * @param projectId unique project identifier (UUIDv7)
     * @param userId authenticated user identifier (UUIDv7)
     * @return the authorized ProjectEntity
     * @throws com.gsir.common.error.GsirException (RESOURCE_NOT_FOUND) if project does not exist
     *         or user is not authorized within the tenant hierarchy
     * @throws com.gsir.common.error.GsirException (INVALID_REQUEST) if arguments are null
     */
    public ProjectEntity requireAccess(UUID projectId, UUID userId) {
        // Defensive validation: ensure non-null identity parameters
        if (projectId == null || userId == null) {
            throw CommonErrors.invalid("Project ID and User ID must not be null");
        }

        // Execute tenant-isolated repository lookup
        return projectRepository
                .findAccessibleProject(projectId, userId)
                .orElseThrow(() -> CommonErrors.notFound("Project"));
    }

    /**
     * Authorizes and returns the requested project while verifying explicit containment
     * against client-supplied organization and workspace identifiers.
     *
     * Prevents cross-workspace and cross-organization hopping even if a user holds valid
     * memberships across multiple disparate tenants.
     *
     * @param organizationId expected organization identifier
     * @param workspaceId expected workspace identifier
     * @param projectId project identifier
     * @param userId authenticated user identifier
     * @return the authorized ProjectEntity
     */
    public ProjectEntity requireAccess(UUID organizationId, UUID workspaceId, UUID projectId, UUID userId) {
        // Defensive validation: all hierarchy coordinates must be present
        if (organizationId == null || workspaceId == null || projectId == null || userId == null) {
            throw CommonErrors.invalid("Organization ID, Workspace ID, Project ID, and User ID must not be null");
        }

        // Verify that the project belongs to the given workspace, the workspace belongs
        // to the given organization, and the user holds memberships in both
        return projectRepository
                .findAccessibleProject(organizationId, workspaceId, projectId, userId)
                .orElseThrow(() -> CommonErrors.notFound("Project"));
    }
}
