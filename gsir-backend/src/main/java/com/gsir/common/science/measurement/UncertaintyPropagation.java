package com.gsir.common.science.measurement;

import com.gsir.common.math.NumericSafety;
import java.util.Optional;

public final class UncertaintyPropagation {

    private UncertaintyPropagation() {}

    public static Optional<Double> propagateAddition(
            ScientificMeasurement m1,
            ScientificMeasurement m2,
            double covariance) {
        return propagate(m1, m2, covariance, 1.0, 1.0);
    }

    public static Optional<Double> propagateSubtraction(
            ScientificMeasurement m1,
            ScientificMeasurement m2,
            double covariance) {
        return propagate(m1, m2, covariance, 1.0, -1.0);
    }

    public static Optional<Double> propagateMultiplication(
            ScientificMeasurement m1,
            ScientificMeasurement m2,
            double covariance) {
        
        if (!m1.hasUncertainty() || !m2.hasUncertainty()) return Optional.empty();
        
        double x = m1.value();
        double y = m2.value();
        return propagate(m1, m2, covariance, y, x);
    }

    public static Optional<Double> propagateDivision(
            ScientificMeasurement m1,
            ScientificMeasurement m2,
            double covariance) {
            
        if (!m1.hasUncertainty() || !m2.hasUncertainty()) return Optional.empty();
        
        double x = m1.value();
        double y = m2.value();
        
        if (y == 0.0) {
            throw new IllegalArgumentException("Division by zero in uncertainty propagation");
        }
        
        double df_dx = 1.0 / y;
        double df_dy = -x / (y * y);
        return propagate(m1, m2, covariance, df_dx, df_dy);
    }

    private static Optional<Double> propagate(
            ScientificMeasurement m1,
            ScientificMeasurement m2,
            double covariance,
            double df_dx1,
            double df_dx2) {

        NumericSafety.requireFinite(covariance);

        if (!m1.hasUncertainty() || !m2.hasUncertainty()) {
            return Optional.empty();
        }

        double u1 = m1.uncertainty();
        double u2 = m2.uncertainty();
        
        NumericSafety.requireFinite(u1);
        NumericSafety.requireFinite(u2);
        
        if (u1 < 0.0 || u2 < 0.0) {
            throw new IllegalArgumentException("Uncertainty cannot be negative");
        }

        double var1 = u1 * u1;
        double var2 = u2 * u2;
        
        // Cauchy-Schwarz bound on covariance: |cov| <= u1 * u2
        if (Math.abs(covariance) > u1 * u2 * 1.00000001) { // 1.00000001 to handle float math slightly
            throw new IllegalArgumentException("Invalid covariance: exceeds Cauchy-Schwarz bound (u1*u2)");
        }

        double var = (df_dx1 * df_dx1 * var1) + (df_dx2 * df_dx2 * var2) + (2 * df_dx1 * df_dx2 * covariance);
        
        if (var < 0) {
            var = 0.0;
        }
        
        double propagatedUncertainty = Math.sqrt(var);
        NumericSafety.requireFinite(propagatedUncertainty);
        return Optional.of(propagatedUncertainty);
    }
}
