package com.gsir.common.math;

import java.util.List;

public final class Statistics {

    private Statistics() {}

    public static double mean(List<Double> values) {
        requireNonEmpty(values);
        return values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElseThrow();
    }

    public static double sampleVariance(List<Double> values) {
        requireAtLeastTwo(values);
        double mean = mean(values);
        double sum = 0.0;
        for (double value : values) {
            double difference = value - mean;
            sum += difference * difference;
        }
        double result = sum / (values.size() - 1);
        
        return result;
    }

    public static double sampleStandardDeviation(List<Double> values) {
        return Math.sqrt(sampleVariance(values));
    }

    private static void requireNonEmpty(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("At least one value is required");
        }
    }

    private static void requireAtLeastTwo(List<Double> values) {
        if (values == null || values.size() < 2) {
            throw new IllegalArgumentException("At least two values are required");
        }
    }
}

