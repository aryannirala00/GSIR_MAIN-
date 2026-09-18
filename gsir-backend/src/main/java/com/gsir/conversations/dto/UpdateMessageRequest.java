package com.gsir.conversations.dto;

import jakarta.validation.constraints.Size;

public record UpdateMessageRequest(
        @Size(max = 32) String status,
        String metadata
) {}

