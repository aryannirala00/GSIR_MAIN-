package com.gsir.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface NoteAttachmentRepository extends JpaRepository<NoteAttachmentEntity, UUID> {
    List<NoteAttachmentEntity> findByNoteId(UUID noteId);
}

