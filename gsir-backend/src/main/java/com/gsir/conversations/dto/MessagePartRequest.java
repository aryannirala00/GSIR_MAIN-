package com.gsir.conversations.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessagePartRequest(
        @NotBlank @Size(max = 32) String partType,
        String content,
        String metadata,
        @NotNull @Min(0) Integer sequenceNumber
) {}

