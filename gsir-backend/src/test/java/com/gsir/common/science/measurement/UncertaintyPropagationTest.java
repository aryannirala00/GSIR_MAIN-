package com.gsir.common.science.measurement;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UncertaintyPropagationTest {

    @Test
    void testMeasurementCreation() {
        ScientificMeasurement m = new ScientificMeasurement(
            10.0, 0.5, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null
        );
        assertThat(m.value()).isEqualTo(10.0);
        assertThat(m.uncertainty()).isEqualTo(0.5);
        assertThat(m.uncertaintyType()).isEqualTo(UncertaintyType.MEASUREMENT_ERROR);
        assertThat(m.hasUncertainty()).isTrue();
    }

    @Test
    void testNegativeUncertaintyThrows() {
        assertThatThrownBy(() -> new ScientificMeasurement(10.0, -0.5, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("negative");
    }

    @Test
    void testNaNUncertaintyThrows() {
        assertThatThrownBy(() -> new ScientificMeasurement(10.0, Double.NaN, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void testInfinityUncertaintyThrows() {
        assertThatThrownBy(() -> new ScientificMeasurement(10.0, Double.POSITIVE_INFINITY, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testZeroUncertainty() {
        ScientificMeasurement m1 = new ScientificMeasurement(10.0, 0.0, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(5.0, 0.4, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        Optional<Double> res = UncertaintyPropagation.propagateAddition(m1, m2, 0.0);
        assertThat(res).isPresent();
        assertThat(res.get()).isCloseTo(0.4, org.assertj.core.data.Offset.offset(0.0001));
    }

    @Test
    void testAdditionPropagation() {
        ScientificMeasurement m1 = new ScientificMeasurement(10.0, 0.3, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(5.0, 0.4, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        Optional<Double> res = UncertaintyPropagation.propagateAddition(m1, m2, 0.0);
        assertThat(res).isPresent();
        assertThat(res.get()).isCloseTo(0.5, org.assertj.core.data.Offset.offset(0.0001));
    }

    @Test
    void testAdditionWithCovariance() {
        ScientificMeasurement m1 = new ScientificMeasurement(10.0, 0.3, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(5.0, 0.4, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        Optional<Double> res = UncertaintyPropagation.propagateAddition(m1, m2, 0.12);
        assertThat(res).isPresent();
        assertThat(res.get()).isCloseTo(0.7, org.assertj.core.data.Offset.offset(0.0001));
    }
    
    @Test
    void testSubtractionPropagation() {
        ScientificMeasurement m1 = new ScientificMeasurement(10.0, 0.3, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(5.0, 0.4, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        // z = x - y
        // var = u1^2 + u2^2 - 2*cov
        // If cov = 0, it's just addition of variances
        Optional<Double> res = UncertaintyPropagation.propagateSubtraction(m1, m2, 0.0);
        assertThat(res).isPresent();
        assertThat(res.get()).isCloseTo(0.5, org.assertj.core.data.Offset.offset(0.0001));
        
        // If cov = 0.12 (highly correlated), var = 0.09 + 0.16 - 0.24 = 0.01 -> std = 0.1
        Optional<Double> res2 = UncertaintyPropagation.propagateSubtraction(m1, m2, 0.12);
        assertThat(res2).isPresent();
        assertThat(res2.get()).isCloseTo(0.1, org.assertj.core.data.Offset.offset(0.0001));
    }
    
    @Test
    void testCauchySchwarzViolation() {
        ScientificMeasurement m1 = new ScientificMeasurement(10.0, 0.3, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(5.0, 0.4, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        // max cov is 0.3 * 0.4 = 0.12. 
        // passing 0.2 should throw
        assertThatThrownBy(() -> UncertaintyPropagation.propagateAddition(m1, m2, 0.2))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testMultiplicationPropagation() {
        ScientificMeasurement m1 = new ScientificMeasurement(10.0, 0.1, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(20.0, 0.2, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        Optional<Double> res = UncertaintyPropagation.propagateMultiplication(m1, m2, 0.0);
        assertThat(res).isPresent();
        assertThat(res.get()).isCloseTo(Math.sqrt(8.0), org.assertj.core.data.Offset.offset(0.0001));
    }

    @Test
    void testMultiplicationWithZeroValue() {
        ScientificMeasurement m1 = new ScientificMeasurement(0.0, 0.1, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(20.0, 0.2, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        Optional<Double> res = UncertaintyPropagation.propagateMultiplication(m1, m2, 0.0);
        assertThat(res).isPresent();
        assertThat(res.get()).isCloseTo(2.0, org.assertj.core.data.Offset.offset(0.0001));
    }
    
    @Test
    void testDivisionPropagation() {
        ScientificMeasurement m1 = new ScientificMeasurement(10.0, 0.1, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(20.0, 0.2, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        Optional<Double> res = UncertaintyPropagation.propagateDivision(m1, m2, 0.0);
        assertThat(res).isPresent();
        // z = x / y
        // var = (1/y)^2 * u1^2 + (-x/y^2)^2 * u2^2
        // y=20, x=10
        // (1/20)^2 * 0.01 + (-10/400)^2 * 0.04
        // (0.05)^2 * 0.01 + (-0.025)^2 * 0.04
        // 0.0025 * 0.01 + 0.000625 * 0.04
        // 0.000025 + 0.000025 = 0.00005
        // sqrt(0.00005) = 0.00707106
        assertThat(res.get()).isCloseTo(0.00707106, org.assertj.core.data.Offset.offset(0.000001));
    }
    
    @Test
    void testDivisionByZeroValue() {
        ScientificMeasurement m1 = new ScientificMeasurement(10.0, 0.1, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(0.0, 0.2, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        assertThatThrownBy(() -> UncertaintyPropagation.propagateDivision(m1, m2, 0.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testNoFabrication() {
        ScientificMeasurement m1 = new ScientificMeasurement(10.0, null, null, "m", "std", "none", null);
        ScientificMeasurement m2 = new ScientificMeasurement(20.0, 0.2, UncertaintyType.MEASUREMENT_ERROR, "m", "std", "none", null);
        
        assertThat(m1.hasUncertainty()).isFalse();
        Optional<Double> res = UncertaintyPropagation.propagateAddition(m1, m2, 0.0);
        assertThat(res).isEmpty();
        
        res = UncertaintyPropagation.propagateMultiplication(m1, m2, 0.0);
        assertThat(res).isEmpty();
    }
}
