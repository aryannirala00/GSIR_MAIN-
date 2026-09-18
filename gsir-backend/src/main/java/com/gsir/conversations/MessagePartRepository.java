package com.gsir.conversations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessagePartRepository extends JpaRepository<MessagePartEntity, UUID> {
    List<MessagePartEntity> findByMessageIdOrderBySequenceNumberAsc(UUID messageId);
}

