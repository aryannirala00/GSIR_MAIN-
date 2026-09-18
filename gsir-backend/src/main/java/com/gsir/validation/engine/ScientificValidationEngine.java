package com.gsir.validation.engine;

import com.gsir.validation.api.ScientificValidator;
import com.gsir.validation.model.ValidationResult;
import com.gsir.common.science.EvidenceStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScientificValidationEngine {

    private final List<ScientificValidator> validators;

    public ScientificValidationEngine(List<ScientificValidator> validators) {
        this.validators = validators != null ? validators : new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public ValidationResult validate(Object entity) {
        if (entity == null) {
            return new ValidationResult(false, EvidenceStatus.UNKNOWN, List.of("Entity is null"), List.of());
        }

        boolean valid = true;
        EvidenceStatus lowestStatus = EvidenceStatus.OBSERVED_FACT;
        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();
        boolean executed = false;

        for (ScientificValidator validator : validators) {
            if (validator.supports(entity.getClass())) {
                executed = true;
                ValidationResult result = validator.validate(entity);
                if (!result.valid()) {
                    valid = false;
                }
                if (result.status() != null && result.status().ordinal() > lowestStatus.ordinal()) {
                    lowestStatus = result.status();
                }
                if (result.errors() != null) {
                    allErrors.addAll(result.errors());
                }
                if (result.warnings() != null) {
                    allWarnings.addAll(result.warnings());
                }
            }
        }

        if (!executed) {
            return new ValidationResult(true, EvidenceStatus.UNVERIFIED, List.of(), List.of("No validators found for " + entity.getClass().getSimpleName()));
        }

        if (!valid && lowestStatus.ordinal() < EvidenceStatus.CONFLICTING.ordinal()) {
            lowestStatus = EvidenceStatus.UNVERIFIED;
        }

        return new ValidationResult(valid, lowestStatus, allErrors, allWarnings);
    }
}
