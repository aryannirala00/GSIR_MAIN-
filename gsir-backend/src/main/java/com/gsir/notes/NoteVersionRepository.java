package com.gsir.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteVersionRepository extends JpaRepository<NoteVersionEntity, UUID> {
    List<NoteVersionEntity> findByNoteIdOrderByVersionNumberDesc(UUID noteId);
}

