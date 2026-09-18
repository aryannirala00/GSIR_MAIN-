package com.gsir.notes;

import com.gsir.common.error.GsirException;
import com.gsir.organizations.OrganizationMembershipId;
import com.gsir.organizations.OrganizationMembershipRepository;
import com.gsir.projects.ProjectEntity;
import com.gsir.projects.ProjectRepository;
import com.gsir.workspaces.WorkspaceEntity;
import com.gsir.workspaces.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class NoteServiceTest {

    private NoteRepository noteRepository;
    private NoteVersionRepository noteVersionRepository;
    private ProjectRepository projectRepository;
    private WorkspaceRepository workspaceRepository;
    private OrganizationMembershipRepository orgMembershipRepo;
    private NoteAttachmentRepository noteAttachmentRepository;
    private NoteRelationshipRepository noteRelationshipRepository;
    private com.gsir.common.id.UuidV7Generator idGenerator;
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        noteRepository = Mockito.mock(NoteRepository.class);
        noteVersionRepository = Mockito.mock(NoteVersionRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        workspaceRepository = Mockito.mock(WorkspaceRepository.class);
        orgMembershipRepo = Mockito.mock(OrganizationMembershipRepository.class);
        noteAttachmentRepository = Mockito.mock(NoteAttachmentRepository.class);
        noteRelationshipRepository = Mockito.mock(NoteRelationshipRepository.class);
        idGenerator = new com.gsir.common.id.UuidV7Generator();

        noteService = new NoteService(
            noteRepository, 
            noteVersionRepository, 
            projectRepository, 
            workspaceRepository, 
            orgMembershipRepo, 
            noteAttachmentRepository, 
            noteRelationshipRepository, 
            idGenerator
        );
    }

    @Test
    void testCreatePersonalNote_Success() {
        UUID userId = UUID.randomUUID();
        NoteRequest req = new NoteRequest("My Note", "Content", null, null, null, null);

        NoteResponse res = noteService.createNote(req, userId);
        
        assertNotNull(res);
        assertEquals("My Note", res.title());
        assertEquals(userId, res.userId());
        assertNull(res.projectId());
    }

    @Test
    void testCreateProjectNote_RequiresProjectAccess() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        NoteRequest req = new NoteRequest("Project Note", "Content", projectId, null, null, null);

        // Mock project access denial
        when(projectRepository.findAccessibleProject(projectId, userId)).thenReturn(Optional.empty());

        assertThrows(GsirException.class, () -> noteService.createNote(req, userId));
    }

    @Test
    void testGetPersonalNote_UnauthorizedUser_ReturnsNotFound() {
        UUID noteId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();

        NoteEntity note = new NoteEntity(noteId, ownerId, "Title", "Content");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        GsirException ex = assertThrows(GsirException.class, () -> noteService.getNote(noteId, attackerId));
        assertEquals("Note was not found", ex.getMessage());
    }

    @Test
    void testGetProjectNote_UnauthorizedUser_ReturnsNotFound() {
        UUID noteId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        NoteEntity note = new NoteEntity(noteId, ownerId, "Title", "Content");
        note.setProjectId(projectId);

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));
        when(projectRepository.findAccessibleProject(projectId, attackerId)).thenReturn(Optional.empty());

        GsirException ex = assertThrows(GsirException.class, () -> noteService.getNote(noteId, attackerId));
        assertEquals("Note was not found", ex.getMessage());
    }

    @Test
    void testValidation_EmptyTitle() {
        UUID userId = UUID.randomUUID();
        NoteRequest req = new NoteRequest("", "Content", null, null, null, null);
        assertThrows(GsirException.class, () -> noteService.createNote(req, userId));
    }
}
