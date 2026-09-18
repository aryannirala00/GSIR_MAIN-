package com.gsir.common.science.measurement;

import com.gsir.common.math.NumericSafety;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public record ScientificMeasurement(
    double value,
    Double uncertainty,
    UncertaintyType uncertaintyType,
    String unit,
    String method,
    String assumptions,
    Map<String, Double> covarianceInfo
) {
    public ScientificMeasurement {
        NumericSafety.requireFinite(value);
        if (uncertainty != null) {
            NumericSafety.requireFinite(uncertainty);
            if (uncertainty < 0.0) {
                throw new IllegalArgumentException("Uncertainty cannot be negative");
            }
        }
        if (uncertaintyType == null) {
            uncertaintyType = UncertaintyType.UNSPECIFIED;
        }
        if (covarianceInfo == null) {
            covarianceInfo = Collections.emptyMap();
        } else {
            covarianceInfo = Map.copyOf(covarianceInfo);
        }
    }

    public boolean hasUncertainty() {
        return uncertainty != null;
    }

    public Optional<Double> getUncertainty() {
        return Optional.ofNullable(uncertainty);
    }
}
