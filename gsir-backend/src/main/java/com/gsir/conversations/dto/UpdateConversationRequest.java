package com.gsir.conversations.dto;

import jakarta.validation.constraints.Size;

public record UpdateConversationRequest(
        @Size(max = 500) String title,
        @Size(max = 32) String status
) {}

