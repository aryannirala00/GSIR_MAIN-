package com.gsir.notes;

import com.gsir.common.error.CommonErrors;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.organizations.OrganizationMembershipId;
import com.gsir.organizations.OrganizationMembershipRepository;
import com.gsir.projects.ProjectRepository;
import com.gsir.workspaces.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteVersionRepository noteVersionRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final OrganizationMembershipRepository orgMembershipRepo;
    private final NoteAttachmentRepository noteAttachmentRepository;
    private final NoteRelationshipRepository noteRelationshipRepository;
    private final com.gsir.common.id.UuidV7Generator idGenerator;

    public NoteService(
            NoteRepository noteRepository,
            NoteVersionRepository noteVersionRepository,
            ProjectRepository projectRepository,
            WorkspaceRepository workspaceRepository,
            OrganizationMembershipRepository orgMembershipRepo,
            NoteAttachmentRepository noteAttachmentRepository,
            NoteRelationshipRepository noteRelationshipRepository,
            com.gsir.common.id.UuidV7Generator idGenerator) {
        this.noteRepository = noteRepository;
        this.noteVersionRepository = noteVersionRepository;
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
        this.orgMembershipRepo = orgMembershipRepo;
        this.noteAttachmentRepository = noteAttachmentRepository;
        this.noteRelationshipRepository = noteRelationshipRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public NoteResponse createNote(NoteRequest request, UUID userId) {
        validateContent(request.title(), request.content());
        validateOwnership(request.projectId(), request.workspaceId(), request.organizationId(), userId);

        UUID noteId = idGenerator.generate();
        NoteEntity note = new NoteEntity(noteId, userId, request.title(), request.content());
        note.setProjectId(request.projectId());
        note.setWorkspaceId(request.workspaceId());
        note.setOrganizationId(request.organizationId());
        
        if (request.tags() != null) {
            note.setTags(request.tags());
        }

        noteRepository.saveAndFlush(note);
        saveVersion(note, userId);

        return NoteResponse.from(note);
    }

    @Transactional
    public NoteResponse updateNote(UUID noteId, NoteRequest request, UUID userId, Integer expectedVersion) {
        NoteEntity note = requireAccess(noteId, userId);
        
        if (expectedVersion != null && !note.getVersionNumber().equals(expectedVersion)) {
            throw CommonErrors.invalid("Version conflict detected. Expected: " + expectedVersion); // Or custom conflict error
        }

        validateContent(request.title(), request.content());
        // Allow updating ownership scopes if user has access to new targets
        if (isScopeChanged(note, request)) {
            validateOwnership(request.projectId(), request.workspaceId(), request.organizationId(), userId);
            note.setProjectId(request.projectId());
            note.setWorkspaceId(request.workspaceId());
            note.setOrganizationId(request.organizationId());
        }

        note.setTitle(request.title());
        note.setContent(request.content());
        
        if (request.tags() != null) {
            note.setTags(request.tags());
        }

        noteRepository.saveAndFlush(note); // Version will auto-increment via @Version if Spring Data JPA configured, else we should manually increment. 
        // Wait, @Version increments automatically on update! But we need to save explicitly to our history table.
        saveVersion(note, userId);

        return NoteResponse.from(note);
    }

    private boolean isScopeChanged(NoteEntity note, NoteRequest request) {
        return !java.util.Objects.equals(note.getProjectId(), request.projectId()) ||
               !java.util.Objects.equals(note.getWorkspaceId(), request.workspaceId()) ||
               !java.util.Objects.equals(note.getOrganizationId(), request.organizationId());
    }

    public NoteResponse getNote(UUID noteId, UUID userId) {
        return NoteResponse.from(requireAccess(noteId, userId));
    }

    @Transactional
    public void deleteNote(UUID noteId, UUID userId) {
        NoteEntity note = requireAccess(noteId, userId);
        noteRepository.delete(note);
    }

    @Transactional
    public NoteResponse archiveNote(UUID noteId, UUID userId) {
        NoteEntity note = requireAccess(noteId, userId);
        note.setArchived(true);
        noteRepository.save(note);
        return NoteResponse.from(note);
    }

    private NoteEntity requireAccess(UUID noteId, UUID userId) {
        NoteEntity note = noteRepository.findById(noteId)
                .orElseThrow(() -> CommonErrors.notFound("Note"));

        // If personal note
        if (note.getProjectId() == null && note.getWorkspaceId() == null && note.getOrganizationId() == null) {
            if (!note.getUserId().equals(userId)) {
                throw CommonErrors.notFound("Note"); // 404 to prevent existence leak
            }
            return note;
        }

        // Validate access through hierarchy
        try {
            validateOwnership(note.getProjectId(), note.getWorkspaceId(), note.getOrganizationId(), userId);
        } catch (Exception e) {
            throw CommonErrors.notFound("Note");
        }
        
        return note;
    }

    private void validateOwnership(UUID projectId, UUID workspaceId, UUID orgId, UUID userId) {
        if (projectId != null) {
            projectRepository.findAccessibleProject(projectId, userId)
                    .orElseThrow(() -> CommonErrors.notFound("Project"));
        } else if (workspaceId != null) {
            workspaceRepository.findAccessibleWorkspace(workspaceId, userId)
                    .orElseThrow(() -> CommonErrors.notFound("Workspace"));
        } else if (orgId != null) {
            if (!orgMembershipRepo.existsById(new OrganizationMembershipId(orgId, userId))) {
                throw CommonErrors.notFound("Organization");
            }
        }
    }

    private void validateContent(String title, String content) {
        if (title == null || title.isBlank() || title.length() > 500) {
            throw CommonErrors.invalid("Title must be between 1 and 500 characters");
        }
        if (content == null || content.isBlank()) {
            throw CommonErrors.invalid("Content must not be empty");
        }
    }

    private void saveVersion(NoteEntity note, UUID userId) {
        NoteVersionEntity version = new NoteVersionEntity(
                idGenerator.generate(),
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getVersionNumber(),
                userId
        );
        noteVersionRepository.save(version);
    }

    public List<NoteVersionEntity> getVersions(UUID noteId, UUID userId) {
        requireAccess(noteId, userId);
        return noteVersionRepository.findByNoteIdOrderByVersionNumberDesc(noteId);
    }

    @Transactional
    public NoteResponse updateNoteSummary(UUID noteId, UUID userId, String summary) {
        NoteEntity note = requireAccess(noteId, userId);
        note.setSummary(summary);
        note.setAiGeneratedSummary(true);
        noteRepository.save(note);
        return NoteResponse.from(note);
    }

    @Transactional
    public NoteResponse restoreNote(UUID noteId, UUID userId) {
        NoteEntity note = requireAccess(noteId, userId);
        note.setArchived(false);
        noteRepository.save(note);
        return NoteResponse.from(note);
    }

    public String exportNote(UUID noteId, UUID userId) {
        NoteEntity note = requireAccess(noteId, userId);
        return "Title: " + note.getTitle() + "\n\n" + note.getContent();
    }

    public List<NoteResponse> searchNotes(String keyword, UUID userId) {
        return noteRepository.searchAuthorizedNotes(userId, keyword).stream()
                .map(NoteResponse::from)
                .toList();
    }

    @Transactional
    public NoteAttachmentEntity addAttachment(UUID noteId, UUID fileId, UUID userId) {
        requireAccess(noteId, userId);
        NoteAttachmentEntity attachment = new NoteAttachmentEntity(idGenerator.generate(), noteId, fileId);
        return noteAttachmentRepository.save(attachment);
    }

    public List<NoteRelationshipEntity> getRelationships(UUID noteId, UUID userId) {
        requireAccess(noteId, userId);
        return noteRelationshipRepository.findByNoteId(noteId);
    }
}
