package com.gsir.conversations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {
    
    @Query("SELECT c FROM ConversationEntity c WHERE c.projectId = :projectId AND c.status != 'DELETED' ORDER BY c.createdAt DESC")
    List<ConversationEntity> findByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT c FROM ConversationEntity c WHERE c.userId = :userId AND c.projectId IS NULL AND c.status != 'DELETED' ORDER BY c.createdAt DESC")
    List<ConversationEntity> findPersonalConversations(@Param("userId") UUID userId);
    
    Optional<ConversationEntity> findByIdAndStatusNot(UUID id, String status);
}

