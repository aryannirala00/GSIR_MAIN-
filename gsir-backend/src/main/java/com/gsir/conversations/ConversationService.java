package com.gsir.conversations;

import com.gsir.common.error.CommonErrors;
import com.gsir.common.id.UuidV7Generator;
import com.gsir.common.idempotency.IdempotencyRecordEntity;
import com.gsir.common.idempotency.IdempotencyRecordId;
import com.gsir.common.idempotency.IdempotencyRecordRepository;
import com.gsir.conversations.dto.*;
import com.gsir.projects.ProjectAccessService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessagePartRepository messagePartRepository;
    private final ProjectAccessService projectAccessService;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final UuidV7Generator idGenerator;
    private final TransactionTemplate transactionTemplate;

    public ConversationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            MessagePartRepository messagePartRepository,
            ProjectAccessService projectAccessService,
            IdempotencyRecordRepository idempotencyRecordRepository,
            UuidV7Generator idGenerator,
            TransactionTemplate transactionTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messagePartRepository = messagePartRepository;
        this.projectAccessService = projectAccessService;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.idGenerator = idGenerator;
        this.transactionTemplate = transactionTemplate;
    }

    public ConversationResponse createConversation(UUID userId, String idempotencyKey, CreateConversationRequest req) {
        if (userId == null || req == null) {
            throw CommonErrors.invalid("userId and request must not be null");
        }
        
        if (req.projectId() != null) {
            projectAccessService.requireAccess(req.projectId(), userId);
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecordId recordId = new IdempotencyRecordId(userId, idempotencyKey);
            Optional<IdempotencyRecordEntity> existingRecord = idempotencyRecordRepository.findById(recordId);
            if (existingRecord.isPresent()) {
                return getConversation(userId, existingRecord.get().getResourceId());
            }
        }

        return executeCreate(userId, idempotencyKey, req);
    }

    private ConversationResponse executeCreate(UUID userId, String idempotencyKey, CreateConversationRequest req) {
        try {
            return transactionTemplate.execute(status -> {
                UUID conversationId = idGenerator.generate();

                if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                    idempotencyRecordRepository.saveAndFlush(
                            new IdempotencyRecordEntity(new IdempotencyRecordId(userId, idempotencyKey), "Conversation", conversationId)
                    );
                }

                ConversationEntity entity = new ConversationEntity(
                        conversationId,
                        userId,
                        req.workspaceId(),
                        req.projectId(),
                        req.title(),
                        req.mode(),
                        "ACTIVE",
                        null,
                        null
                );

                ConversationEntity saved = conversationRepository.save(entity);
                return ConversationResponse.fromEntity(saved);
            });
        } catch (DataIntegrityViolationException race) {
            if (idempotencyKey != null) {
                return idempotencyRecordRepository
                        .findById(new IdempotencyRecordId(userId, idempotencyKey))
                        .map(rec -> getConversation(userId, rec.getResourceId()))
                        .orElseThrow(() -> CommonErrors.conflict("Concurrent conflict during conversation creation"));
            }
            throw CommonErrors.conflict("Concurrent conflict during conversation creation");
        }
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID userId, UUID conversationId) {
        ConversationEntity conversation = getAccessibleConversation(userId, conversationId);
        return ConversationResponse.fromEntity(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listConversations(UUID userId, UUID projectId) {
        List<ConversationEntity> entities;
        if (projectId != null) {
            projectAccessService.requireAccess(projectId, userId);
            entities = conversationRepository.findByProjectId(projectId);
        } else {
            entities = conversationRepository.findPersonalConversations(userId);
        }
        return entities.stream().map(ConversationResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public ConversationResponse updateConversation(UUID userId, UUID conversationId, UpdateConversationRequest req) {
        ConversationEntity conversation = getAccessibleConversation(userId, conversationId);
        
        if (req.title() != null) {
            conversation.setTitle(req.title());
        }
        if (req.status() != null) {
            conversation.setStatus(req.status());
            if ("ARCHIVED".equals(req.status())) {
                conversation.setArchivedAt(Instant.now());
            }
        }
        
        return ConversationResponse.fromEntity(conversationRepository.save(conversation));
    }

    @Transactional
    public void deleteConversation(UUID userId, UUID conversationId) {
        ConversationEntity conversation = getAccessibleConversation(userId, conversationId);
        conversation.setStatus("DELETED");
        conversationRepository.save(conversation);
    }

    @Transactional
    public MessageResponse createMessage(UUID userId, UUID conversationId, CreateMessageRequest req) {
        ConversationEntity conversation = getAccessibleConversation(userId, conversationId);
        
        if (req.parentMessageId() != null) {
            messageRepository.findByIdAndStatusNot(req.parentMessageId(), "DELETED")
                    .orElseThrow(() -> CommonErrors.notFound("Parent Message"));
        }

        UUID messageId = idGenerator.generate();
        MessageEntity message = new MessageEntity(
                messageId,
                conversationId,
                req.parentMessageId(),
                req.role(),
                "COMPLETED",
                req.modelId(),
                req.metadata()
        );
        MessageEntity savedMessage = messageRepository.save(message);

        List<MessagePartResponse> partResponses = req.parts().stream().map(partReq -> {
            MessagePartEntity part = new MessagePartEntity(
                    idGenerator.generate(),
                    messageId,
                    partReq.partType(),
                    partReq.content(),
                    partReq.metadata(),
                    partReq.sequenceNumber()
            );
            return MessagePartResponse.fromEntity(messagePartRepository.save(part));
        }).collect(Collectors.toList());

        return MessageResponse.fromEntity(savedMessage, partResponses);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(UUID userId, UUID conversationId) {
        ConversationEntity conversation = getAccessibleConversation(userId, conversationId);
        
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .filter(m -> !"DELETED".equals(m.getStatus()))
                .map(m -> {
                    List<MessagePartResponse> parts = messagePartRepository.findByMessageIdOrderBySequenceNumberAsc(m.getId())
                            .stream().map(MessagePartResponse::fromEntity).collect(Collectors.toList());
                    return MessageResponse.fromEntity(m, parts);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse updateMessage(UUID userId, UUID messageId, UpdateMessageRequest req) {
        MessageEntity message = getAccessibleMessage(userId, messageId);
        
        if (req.status() != null) {
            message.setStatus(req.status());
        }
        if (req.metadata() != null) {
            message.setMetadata(req.metadata());
        }
        
        MessageEntity saved = messageRepository.save(message);
        List<MessagePartResponse> parts = messagePartRepository.findByMessageIdOrderBySequenceNumberAsc(saved.getId())
                .stream().map(MessagePartResponse::fromEntity).collect(Collectors.toList());
                
        return MessageResponse.fromEntity(saved, parts);
    }

    @Transactional
    public void deleteMessage(UUID userId, UUID messageId) {
        MessageEntity message = getAccessibleMessage(userId, messageId);
        message.setStatus("DELETED");
        messageRepository.save(message);
    }

    private ConversationEntity getAccessibleConversation(UUID userId, UUID conversationId) {
        ConversationEntity conversation = conversationRepository.findByIdAndStatusNot(conversationId, "DELETED")
                .orElseThrow(() -> CommonErrors.notFound("Conversation"));

        if (conversation.getProjectId() != null) {
            try {
                projectAccessService.requireAccess(conversation.getProjectId(), userId);
            } catch (Exception e) {
                throw CommonErrors.notFound("Conversation");
            }
        } else {
            if (!conversation.getUserId().equals(userId)) {
                throw CommonErrors.notFound("Conversation");
            }
        }
        return conversation;
    }
    
    private MessageEntity getAccessibleMessage(UUID userId, UUID messageId) {
        MessageEntity message = messageRepository.findByIdAndStatusNot(messageId, "DELETED")
                .orElseThrow(() -> CommonErrors.notFound("Message"));
        getAccessibleConversation(userId, message.getConversationId());
        return message;
    }
}
