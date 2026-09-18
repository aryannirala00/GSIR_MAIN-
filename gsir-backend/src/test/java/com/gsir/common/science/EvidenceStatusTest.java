package com.gsir.common.science;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

public class EvidenceStatusTest {

    @Test
    @DisplayName("EvidenceStatus must strictly contain only the exactly 8 required values from Part 15")
    public void testExactEnumValues() {
        String[] expected = {
            "OBSERVED_FACT",
            "CALCULATED_RESULT",
            "SUPPORTED_FINDING",
            "INFERENCE",
            "HYPOTHESIS",
            "UNKNOWN",
            "CONFLICTING",
            "UNVERIFIED"
        };
        
        String[] actual = Arrays.stream(EvidenceStatus.values())
                                .map(Enum::name)
                                .toArray(String[]::new);
                                
        // This validates no missing, no extras, and exact spelling/casing
        assertThat(actual).containsExactlyInAnyOrder(expected);
        assertThat(actual).hasSize(8);
    }
}

