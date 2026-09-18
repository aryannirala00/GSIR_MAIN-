package com.gsir.notes;

import com.gsir.common.id.UuidV7Generator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class NoteKnowledgeService {

    private final NoteService noteService;
    private final KnowledgeCandidateRepository candidateRepository;
    private final UuidV7Generator idGenerator;

    public NoteKnowledgeService(NoteService noteService, KnowledgeCandidateRepository candidateRepository, UuidV7Generator idGenerator) {
        this.noteService = noteService;
        this.candidateRepository = candidateRepository;
        this.idGenerator = idGenerator;
    }

    public KnowledgeCandidateEntity promoteToKnowledge(UUID noteId, UUID userId) {
        NoteResponse note = noteService.getNote(noteId, userId);
        
        // 19.25 KNOWLEDGE PROMOTION RULE
        // Preserve provenance explicitly
        String provenance = String.format("Promoted from Note %s (Version %d) at %s", 
                note.id(), note.versionNumber(), java.time.Instant.now().toString());
                
        KnowledgeCandidateEntity candidate = new KnowledgeCandidateEntity(
                idGenerator.generate(),
                note.id(),
                note.content(),
                "PENDING_VALIDATION",
                provenance,
                userId
        );
        
        return candidateRepository.save(candidate);
    }
}
