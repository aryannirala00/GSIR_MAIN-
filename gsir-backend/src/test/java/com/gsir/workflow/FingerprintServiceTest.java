package com.gsir.workflow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FingerprintServiceTest {

    private final FingerprintService service = new FingerprintService();

    @Test
    void testDeterminism() {
        String hash1 = service.generateFingerprint("proj1", "TOOL", "1.0", "{\"a\": 1}");
        String hash2 = service.generateFingerprint("proj1", "TOOL", "1.0", "{\"a\": 1}");
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void testChangeChangesFingerprint() {
        String hash1 = service.generateFingerprint("proj1", "TOOL", "1.0", "{\"a\": 1}");
        String hash2 = service.generateFingerprint("proj1", "TOOL", "1.1", "{\"a\": 1}");
        assertThat(hash1).isNotEqualTo(hash2);
    }
}
