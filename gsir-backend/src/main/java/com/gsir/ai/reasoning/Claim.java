package com.gsir.ai.reasoning;

import java.util.List;

public record Claim(
    String statement,
    double confidence,
    List<String> supportingSources,
    List<String> contradictingSources,
    String status
) {}
