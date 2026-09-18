package com.gsir.conversations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateConversationRequest(
        UUID projectId,
        UUID workspaceId,
        @Size(max = 500) String title,
        @NotBlank @Size(max = 32) String mode
) {}

