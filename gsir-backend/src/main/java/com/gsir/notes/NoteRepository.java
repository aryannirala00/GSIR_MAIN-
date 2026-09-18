package com.gsir.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {
    Optional<NoteEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
        SELECT n
        FROM Note n
        WHERE (
            (n.projectId IS NULL AND n.workspaceId IS NULL AND n.organizationId IS NULL AND n.userId = :userId)
            OR
            (n.projectId IS NOT NULL AND EXISTS (
                SELECT 1 FROM Project p
                JOIN Workspace w ON w.id = p.workspaceId
                JOIN WorkspaceMembership wm ON wm.id.workspaceId = w.id
                JOIN OrganizationMembership om ON om.id.organizationId = w.organizationId AND om.id.userId = wm.id.userId
                WHERE p.id = n.projectId AND wm.id.userId = :userId
            ))
            OR
            (n.workspaceId IS NOT NULL AND EXISTS (
                SELECT 1 FROM Workspace w
                JOIN WorkspaceMembership wm ON wm.id.workspaceId = w.id
                JOIN OrganizationMembership om ON om.id.organizationId = w.organizationId AND om.id.userId = wm.id.userId
                WHERE w.id = n.workspaceId AND wm.id.userId = :userId
            ))
            OR
            (n.organizationId IS NOT NULL AND EXISTS (
                SELECT 1 FROM OrganizationMembership om
                WHERE om.id.organizationId = n.organizationId AND om.id.userId = :userId
            ))
        )
        AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND n.isArchived = false
    """)
    List<NoteEntity> searchAuthorizedNotes(@Param("userId") UUID userId, @Param("keyword") String keyword);
}

