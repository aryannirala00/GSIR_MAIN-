package com.gsir.files.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record FileRequest(
        UUID projectId,

        @NotBlank(message = "File name must not be blank")
        @Size(max = 500, message = "File name must not exceed 500 characters")
        String name,

        String mimeType,

        @NotNull(message = "File size must not be null")
        Long sizeBytes
) {}
