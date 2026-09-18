package com.gsir.validation.engine;

import com.gsir.knowledge.model.ScientificClaim;
import com.gsir.validation.api.ScientificValidator;
import com.gsir.validation.model.ValidationResult;
import com.gsir.common.science.EvidenceStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ScientificClaimValidator implements ScientificValidator<ScientificClaim> {

    @Override
    public boolean supports(Class<?> clazz) {
        return ScientificClaim.class.isAssignableFrom(clazz);
    }

    @Override
    public ValidationResult validate(ScientificClaim claim) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean valid = true;
        EvidenceStatus status = EvidenceStatus.UNVERIFIED;

        if (claim.statement() == null || claim.statement().isBlank()) {
            errors.add("Claim statement is empty");
            valid = false;
        }

        if (claim.confidence() != null) {
            try {
                com.gsir.common.math.NumericSafety.requireFinite(claim.confidence());
                if (claim.confidence() < 0.0 || claim.confidence() > 1.0) {
                    errors.add("Confidence must be between 0.0 and 1.0");
                    valid = false;
                }
            } catch (IllegalArgumentException e) {
                errors.add("Confidence is not a finite number: " + claim.confidence());
                valid = false;
            }
        }

        if (claim.evidence() == null || claim.evidence().isEmpty()) {
            warnings.add("Claim has no evidence attached");
            if (claim.claimType() == com.gsir.knowledge.model.ClaimType.OBSERVED_FACT) {
                errors.add("OBSERVED_FACT must have evidence");
                valid = false;
            }
        } else {
            status = EvidenceStatus.SUPPORTED_FINDING;
        }

        // Check for contradiction
        if (claim.status() == com.gsir.knowledge.model.ClaimStatus.CONTRADICTED) {
            status = EvidenceStatus.CONFLICTING;
            warnings.add("Claim is contradicted by other evidence");
            valid = false;
        }

        return new ValidationResult(valid, status, errors, warnings);
    }
}

