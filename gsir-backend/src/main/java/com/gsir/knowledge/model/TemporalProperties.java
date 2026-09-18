package com.gsir.knowledge.model;

import java.time.Instant;

public record TemporalProperties(
    Instant validFrom,
    Instant validUntil,
    Instant observedAt,
    Instant publishedAt,
    Instant versionTime
) {}
