package com.gsir.notes;

import com.gsir.common.api.ApiResponse;
import com.gsir.common.api.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {

    private final NoteService noteService;
    private final NoteAiService noteAiService;
    private final NoteKnowledgeService knowledgeService;

    public NoteController(NoteService noteService, NoteAiService noteAiService, NoteKnowledgeService knowledgeService) {
        this.noteService = noteService;
        this.noteAiService = noteAiService;
        this.knowledgeService = knowledgeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @RequestBody NoteRequest request,
            @AuthenticationPrincipal UUID userId) {
        NoteResponse note = noteService.createNote(request, userId);
        return ResponseEntity.created(URI.create("/api/v1/notes/" + note.id())).body(ApiResponse.ok(note));
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponse>> getNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.getNote(noteId, userId)));
    }

    @PatchMapping("/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponse>> updateNote(
            @PathVariable UUID noteId,
            @RequestParam(required = false) Integer expectedVersion,
            @RequestBody NoteRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.updateNote(noteId, request, userId, expectedVersion)));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID userId) {
        noteService.deleteNote(noteId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{noteId}/archive")
    public ResponseEntity<ApiResponse<NoteResponse>> archiveNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.archiveNote(noteId, userId)));
    }

    @GetMapping("/{noteId}/versions")
    public ResponseEntity<ApiResponse<PageResponse<NoteVersionEntity>>> getVersions(
            @PathVariable UUID noteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UUID userId) {
        List<NoteVersionEntity> versions = noteService.getVersions(noteId, userId);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(versions, page, size)));
    }

    @PostMapping("/{noteId}/ai/summarize")
    public ResponseEntity<ApiResponse<NoteResponse>> summarizeNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(noteAiService.summarize(noteId, userId)));
    }

    @PostMapping("/{noteId}/ai/improve")
    public ResponseEntity<ApiResponse<NoteResponse>> improveNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(noteAiService.improve(noteId, userId)));
    }

    @PostMapping("/{noteId}/ai/extract-knowledge")
    public ResponseEntity<ApiResponse<String>> extractKnowledge(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(noteAiService.extractKnowledge(noteId, userId)));
    }

    @PostMapping("/{noteId}/promote")
    public ResponseEntity<ApiResponse<KnowledgeCandidateEntity>> promoteNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(knowledgeService.promoteToKnowledge(noteId, userId)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> searchNotes(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UUID userId) {
        List<NoteResponse> results = noteService.searchNotes(query, userId);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(results, page, size)));
    }

    @PostMapping("/{noteId}/restore")
    public ResponseEntity<ApiResponse<NoteResponse>> restoreNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.restoreNote(noteId, userId)));
    }

    @PostMapping("/{noteId}/export")
    public ResponseEntity<ApiResponse<String>> exportNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.exportNote(noteId, userId)));
    }

    @PostMapping("/{noteId}/attachments")
    public ResponseEntity<ApiResponse<NoteAttachmentEntity>> addAttachment(
            @PathVariable UUID noteId,
            @RequestParam UUID fileId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.addAttachment(noteId, fileId, userId)));
    }

    @GetMapping("/{noteId}/relationships")
    public ResponseEntity<ApiResponse<PageResponse<NoteRelationshipEntity>>> getRelationships(
            @PathVariable UUID noteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UUID userId) {
        List<NoteRelationshipEntity> relations = noteService.getRelationships(noteId, userId);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(relations, page, size)));
    }
}
