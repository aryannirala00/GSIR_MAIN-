package com.gsir.conversations;

import com.gsir.common.id.UuidV7Generator;
import com.gsir.common.idempotency.IdempotencyRecordRepository;
import com.gsir.conversations.dto.*;
import com.gsir.projects.ProjectAccessService;
import com.gsir.projects.ProjectRepository;
import com.gsir.projects.ProjectEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ConversationServiceTest {

    private ConversationRepository conversationRepository;
    private MessageRepository messageRepository;
    private MessagePartRepository messagePartRepository;
    private ProjectRepository projectRepository;
    private ProjectAccessService projectAccessService;
    private IdempotencyRecordRepository idempotencyRecordRepository;
    private UuidV7Generator idGenerator;

    private ConversationService service;

    @BeforeEach
    void setUp() {
        conversationRepository = Mockito.mock(ConversationRepository.class);
        messageRepository = Mockito.mock(MessageRepository.class);
        messagePartRepository = Mockito.mock(MessagePartRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        idempotencyRecordRepository = Mockito.mock(IdempotencyRecordRepository.class);
        idGenerator = new UuidV7Generator();
        
        projectAccessService = new ProjectAccessService(projectRepository);

        TransactionTemplate txTemplate = new TransactionTemplate() {
            @Override
            public <T> T execute(org.springframework.transaction.support.TransactionCallback<T> action) {
                return action.doInTransaction(new SimpleTransactionStatus());
            }
        };

        service = new ConversationService(
                conversationRepository,
                messageRepository,
                messagePartRepository,
                projectAccessService,
                idempotencyRecordRepository,
                idGenerator,
                txTemplate
        );
    }

    @Test
    void createConversation_Success() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        
        when(projectRepository.findAccessibleProject(projectId, userId)).thenReturn(Optional.of(new ProjectEntity(projectId, UUID.randomUUID(), "Test", "Test")));
        
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateConversationRequest req = new CreateConversationRequest(projectId, null, "Test", "AUTOMATIC");
        ConversationResponse res = service.createConversation(userId, null, req);

        assertNotNull(res);
        assertNotNull(res.id());
    }

    @Test
    void getConversation_NotFound_Returns404() {
        UUID userId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        
        when(conversationRepository.findByIdAndStatusNot(convId, "DELETED")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getConversation(userId, convId));
    }

    @Test
    void createMessage_Success() {
        UUID userId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        UUID msgId = UUID.randomUUID();
        
        ConversationEntity conv = new ConversationEntity();
        conv.setId(convId);
        conv.setUserId(userId);
        conv.setMode("AUTOMATIC");
        conv.setStatus("ACTIVE");
        
        when(conversationRepository.findByIdAndStatusNot(convId, "DELETED")).thenReturn(Optional.of(conv));
        
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(messagePartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateMessageRequest req = new CreateMessageRequest(null, "USER", null, null, 
                List.of(new MessagePartRequest("TEXT", "Hello", null, 0)));
                
        MessageResponse res = service.createMessage(userId, convId, req);

        assertNotNull(res);
        assertNotNull(res.id());
        assertEquals(1, res.parts().size());
    }
}
