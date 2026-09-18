package com.gsir.common.math;

public final class NumericSafety {

    private NumericSafety() {}

    public static double requireFinite(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Value must be finite");
        }
        return value;
    }

    public static double requirePositive(double value) {
        requireFinite(value);
        if (value <= 0.0) {
            throw new IllegalArgumentException("Value must be positive");
        }
        return value;
    }

    public static double safeDivide(double numerator, double denominator) {
        requireFinite(numerator);
        requireFinite(denominator);
        if (denominator == 0.0) {
            throw new ArithmeticException("Division by zero");
        }
        return numerator / denominator;
    }

    public static double safeSqrt(double value) {
        requireFinite(value);
        if (value < 0.0) {
            throw new IllegalArgumentException("Square-root domain violation");
        }
        return Math.sqrt(value);
    }

    public static double safeLog(double value) {
        requirePositive(value);
        return Math.log(value);
    }
}

