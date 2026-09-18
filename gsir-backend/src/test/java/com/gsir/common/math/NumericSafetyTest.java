package com.gsir.common.math;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class NumericSafetyTest {

    @Test
    void requireFinite_validValues() {
        assertThat(NumericSafety.requireFinite(0.0)).isEqualTo(0.0);
        assertThat(NumericSafety.requireFinite(-0.0)).isEqualTo(-0.0);
        assertThat(NumericSafety.requireFinite(42.5)).isEqualTo(42.5);
        assertThat(NumericSafety.requireFinite(-42.5)).isEqualTo(-42.5);
        assertThat(NumericSafety.requireFinite(Double.MAX_VALUE)).isEqualTo(Double.MAX_VALUE);
        assertThat(NumericSafety.requireFinite(-Double.MAX_VALUE)).isEqualTo(-Double.MAX_VALUE);
        assertThat(NumericSafety.requireFinite(Double.MIN_VALUE)).isEqualTo(Double.MIN_VALUE);
    }

    @Test
    void requireFinite_invalidValues() {
        assertThatThrownBy(() -> NumericSafety.requireFinite(Double.NaN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be finite");

        assertThatThrownBy(() -> NumericSafety.requireFinite(Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be finite");

        assertThatThrownBy(() -> NumericSafety.requireFinite(Double.NEGATIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be finite");
    }

    @Test
    void requirePositive_validValues() {
        assertThat(NumericSafety.requirePositive(0.0001)).isEqualTo(0.0001);
        assertThat(NumericSafety.requirePositive(42.5)).isEqualTo(42.5);
        assertThat(NumericSafety.requirePositive(Double.MAX_VALUE)).isEqualTo(Double.MAX_VALUE);
        assertThat(NumericSafety.requirePositive(Double.MIN_VALUE)).isEqualTo(Double.MIN_VALUE);
    }

    @Test
    void requirePositive_invalidValues() {
        assertThatThrownBy(() -> NumericSafety.requirePositive(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be positive");

        assertThatThrownBy(() -> NumericSafety.requirePositive(-0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be positive");

        assertThatThrownBy(() -> NumericSafety.requirePositive(-42.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be positive");
    }

    @Test
    void requirePositive_nonFinite() {
        assertThatThrownBy(() -> NumericSafety.requirePositive(Double.NaN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be finite");

        assertThatThrownBy(() -> NumericSafety.requirePositive(Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be finite");
    }

    @Test
    void safeDivide_validValues() {
        assertThat(NumericSafety.safeDivide(10.0, 2.0)).isEqualTo(5.0);
        assertThat(NumericSafety.safeDivide(-10.0, 2.0)).isEqualTo(-5.0);
        assertThat(NumericSafety.safeDivide(10.0, -2.0)).isEqualTo(-5.0);
        assertThat(NumericSafety.safeDivide(-10.0, -2.0)).isEqualTo(5.0);
        assertThat(NumericSafety.safeDivide(0.0, 5.0)).isEqualTo(0.0);
    }

    @Test
    void safeDivide_divisionByZero() {
        assertThatThrownBy(() -> NumericSafety.safeDivide(10.0, 0.0))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Division by zero");

        assertThatThrownBy(() -> NumericSafety.safeDivide(10.0, -0.0))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Division by zero");
    }

    @Test
    void safeDivide_nonFiniteInputs() {
        assertThatThrownBy(() -> NumericSafety.safeDivide(Double.NaN, 2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be finite");

        assertThatThrownBy(() -> NumericSafety.safeDivide(10.0, Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be finite");
    }

    @Test
    void safeSqrt_validValues() {
        assertThat(NumericSafety.safeSqrt(4.0)).isEqualTo(2.0);
        assertThat(NumericSafety.safeSqrt(0.0)).isEqualTo(0.0);
        assertThat(NumericSafety.safeSqrt(Double.MAX_VALUE)).isCloseTo(1.3407807929942597E154, within(1E140));
    }

    @Test
    void safeSqrt_invalidDomain() {
        assertThatThrownBy(() -> NumericSafety.safeSqrt(-0.0001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Square-root domain violation");

        assertThatThrownBy(() -> NumericSafety.safeSqrt(-4.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Square-root domain violation");
    }

    @Test
    void safeSqrt_nonFinite() {
        assertThatThrownBy(() -> NumericSafety.safeSqrt(Double.NaN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be finite");

        assertThatThrownBy(() -> NumericSafety.safeSqrt(Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be finite");
    }

    @Test
    void safeLog_validValues() {
        assertThat(NumericSafety.safeLog(Math.E)).isEqualTo(1.0);
        assertThat(NumericSafety.safeLog(1.0)).isEqualTo(0.0);
    }

    @Test
    void safeLog_invalidDomain() {
        assertThatThrownBy(() -> NumericSafety.safeLog(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be positive");

        assertThatThrownBy(() -> NumericSafety.safeLog(-0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be positive");

        assertThatThrownBy(() -> NumericSafety.safeLog(-1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be positive");
    }
}

