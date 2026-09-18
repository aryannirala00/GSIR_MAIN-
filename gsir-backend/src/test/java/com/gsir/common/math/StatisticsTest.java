package com.gsir.common.math;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class StatisticsTest {

    @Test
    void mean_normalCase() {
        List<Double> values = Arrays.asList(2.0, 4.0, 6.0);
        assertThat(Statistics.mean(values)).isEqualTo(4.0);
    }

    @Test
    void mean_withNegativesAndDecimals() {
        List<Double> values = Arrays.asList(-2.5, 0.0, 2.5, 10.0);
        assertThat(Statistics.mean(values)).isEqualTo(2.5);
    }

    @Test
    void mean_singleElement() {
        List<Double> values = Collections.singletonList(42.0);
        assertThat(Statistics.mean(values)).isEqualTo(42.0);
    }

    @Test
    void mean_emptyListThrows() {
        assertThatThrownBy(() -> Statistics.mean(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one value is required");
    }

    @Test
    void mean_nullListThrows() {
        assertThatThrownBy(() -> Statistics.mean(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one value is required");
    }
    
    @Test
    void mean_nullElementThrows() {
        List<Double> values = Arrays.asList(1.0, null, 3.0);
        assertThatThrownBy(() -> Statistics.mean(values))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void sampleVariance_normalCase() {
        // Values: 2.0, 4.0, 6.0 => Mean: 4.0
        // Differences from mean: -2.0, 0.0, 2.0
        // Squares: 4.0, 0.0, 4.0 => Sum: 8.0
        // N - 1 = 2
        // Variance = 8.0 / 2 = 4.0
        List<Double> values = Arrays.asList(2.0, 4.0, 6.0);
        assertThat(Statistics.sampleVariance(values)).isEqualTo(4.0);
    }

    @Test
    void sampleVariance_knownExpectedValues() {
        // Expected values calculated manually or via known tools (like R/Python)
        // Values: 1.5, 2.5, 3.5, 4.5, 5.5
        // Mean = 3.5
        // Sum of squares = (-2)^2 + (-1)^2 + 0 + 1^2 + 2^2 = 4 + 1 + 0 + 1 + 4 = 10
        // Variance = 10 / (5 - 1) = 2.5
        List<Double> values = Arrays.asList(1.5, 2.5, 3.5, 4.5, 5.5);
        assertThat(Statistics.sampleVariance(values)).isCloseTo(2.5, within(1e-10));
    }

    @Test
    void sampleVariance_singleElementThrows() {
        assertThatThrownBy(() -> Statistics.sampleVariance(Collections.singletonList(5.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least two values are required");
    }

    @Test
    void sampleVariance_emptyListThrows() {
        assertThatThrownBy(() -> Statistics.sampleVariance(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least two values are required");
    }
    
    @Test
    void sampleVariance_nullListThrows() {
        assertThatThrownBy(() -> Statistics.sampleVariance(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least two values are required");
    }

    @Test
    void sampleStandardDeviation_normalCase() {
        List<Double> values = Arrays.asList(2.0, 4.0, 6.0);
        // Variance is 4.0, so StdDev should be 2.0
        assertThat(Statistics.sampleStandardDeviation(values)).isEqualTo(2.0);
    }

    @Test
    void sampleStandardDeviation_knownExpectedValues() {
        List<Double> values = Arrays.asList(1.5, 2.5, 3.5, 4.5, 5.5);
        // Variance is 2.5, StdDev is sqrt(2.5) ~ 1.58113883008
        assertThat(Statistics.sampleStandardDeviation(values))
                .isCloseTo(1.58113883008, within(1e-10));
    }

    @Test
    void sampleStandardDeviation_singleElementThrows() {
        assertThatThrownBy(() -> Statistics.sampleStandardDeviation(Collections.singletonList(5.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least two values are required");
    }
}
