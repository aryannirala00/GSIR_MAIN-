package com.gsir.conversations;

import com.gsir.common.api.ApiResponse;
import com.gsir.common.api.PageResponse;
import com.gsir.conversations.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ConversationResponse> createConversation(
            @AuthenticationPrincipal UUID userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateConversationRequest req) {
        return ApiResponse.ok(conversationService.createConversation(userId, idempotencyKey, req));
    }

    @GetMapping("/conversations")
    public ApiResponse<PageResponse<ConversationResponse>> listConversations(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ConversationResponse> conversations = conversationService.listConversations(userId, projectId);
        return ApiResponse.ok(PageResponse.of(conversations, page, size));
    }

    @GetMapping("/conversations/{conversationId}")
    public ApiResponse<ConversationResponse> getConversation(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID conversationId) {
        return ApiResponse.ok(conversationService.getConversation(userId, conversationId));
    }

    @PatchMapping("/conversations/{conversationId}")
    public ApiResponse<ConversationResponse> updateConversation(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID conversationId,
            @Valid @RequestBody UpdateConversationRequest req) {
        return ApiResponse.ok(conversationService.updateConversation(userId, conversationId, req));
    }

    @DeleteMapping("/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID conversationId) {
        conversationService.deleteConversation(userId, conversationId);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MessageResponse> createMessage(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID conversationId,
            @Valid @RequestBody CreateMessageRequest req) {
        return ApiResponse.ok(conversationService.createMessage(userId, conversationId, req));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<PageResponse<MessageResponse>> listMessages(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<MessageResponse> messages = conversationService.listMessages(userId, conversationId);
        return ApiResponse.ok(PageResponse.of(messages, page, size));
    }

    @PatchMapping("/messages/{messageId}")
    public ApiResponse<MessageResponse> updateMessage(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID messageId,
            @Valid @RequestBody UpdateMessageRequest req) {
        return ApiResponse.ok(conversationService.updateMessage(userId, messageId, req));
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID messageId) {
        conversationService.deleteMessage(userId, messageId);
    }
}
