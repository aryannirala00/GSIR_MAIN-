package com.gsir.validation.model;

import com.gsir.common.science.EvidenceStatus;
import java.util.List;

public record ValidationResult(
        boolean valid,
        EvidenceStatus status,
        List<String> errors,
        List<String> warnings) {
}

