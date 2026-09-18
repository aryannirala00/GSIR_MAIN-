package com.gsir.notes;

import java.util.Set;
import java.util.UUID;

public record NoteRequest(
    String title,
    String content,
    UUID projectId,
    UUID workspaceId,
    UUID organizationId,
    Set<String> tags
) {}

