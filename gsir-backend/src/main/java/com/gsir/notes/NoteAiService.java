package com.gsir.notes;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional
public class NoteAiService {

    private final NoteService noteService;

    public NoteAiService(NoteService noteService) {
        this.noteService = noteService;
    }

    public NoteResponse summarize(UUID noteId, UUID userId) {
        NoteResponse note = noteService.getNote(noteId, userId);
        String summary = "AI Summary of: " + sanitizeForAi(note.title());
        return noteService.updateNoteSummary(noteId, userId, summary);
    }

    public NoteResponse improve(UUID noteId, UUID userId) {
        NoteResponse note = noteService.getNote(noteId, userId);
        String improvedContent = "AI Improved Content:\n" + sanitizeForAi(note.content());
        NoteRequest req = new NoteRequest(note.title(), improvedContent, note.projectId(), note.workspaceId(), note.organizationId(), note.tags());
        return noteService.updateNote(noteId, req, userId, note.versionNumber());
    }

    public String extractKnowledge(UUID noteId, UUID userId) {
        NoteResponse note = noteService.getNote(noteId, userId);
        return "AI Extracted Knowledge from: " + sanitizeForAi(note.content());
    }

    private String sanitizeForAi(String input) {
        // Enforces rule 19.52: "The note must not override system or platform instructions."
        // We simulate marking it as data so the LLM doesn't treat it as prompt instructions.
        return "```data\n" + input + "\n```";
    }
}
