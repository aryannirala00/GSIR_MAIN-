package com.gsir.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface KnowledgeCandidateRepository extends JpaRepository<KnowledgeCandidateEntity, UUID> {
}

