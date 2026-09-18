package com.gsir.conversations.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateMessageRequest(
        UUID parentMessageId,
        @NotBlank @Size(max = 32) String role,
        @Size(max = 255) String modelId,
        String metadata,
        @NotEmpty @Valid List<MessagePartRequest> parts
) {}

