package com.gsir.common.id;

import com.gsir.common.persistence.IdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UuidV7GeneratorTest {

    private final UuidV7Generator generator = new UuidV7Generator();

    @Test
    @DisplayName("Generated UUID must satisfy RFC 9562 Version 7 and Variant 2 specifications")
    void testGenerateVersionAndVariant() {
        UUID uuid = generator.generate();

        assertThat(uuid).isNotNull();
        assertThat(uuid.version())
                .as("UUID version must be 7")
                .isEqualTo(7);
        assertThat(uuid.variant())
                .as("UUID variant must be 2 (RFC 4122/9562 IETF standard)")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Generated UUIDs with increasing timestamps must be monotonically ordered")
    void testMonotonicTimeOrdering() {
        long t1 = 1773000000000L;
        long t2 = 1773000001000L;

        UUID id1 = generator.generate(t1);
        UUID id2 = generator.generate(t2);

        assertThat(id1.compareTo(id2))
                .as("Earlier timestamp UUID must compare less than later timestamp UUID")
                .isLessThan(0);

        assertThat(UuidV7Generator.extractTimestamp(id1)).isEqualTo(t1);
        assertThat(UuidV7Generator.extractTimestamp(id2)).isEqualTo(t2);
    }

    @Test
    @DisplayName("Timestamp extraction should match generation timestamp")
    void testTimestampExtraction() {
        long now = Instant.now().toEpochMilli();
        UUID uuid = generator.generate(now);

        long extracted = UuidV7Generator.extractTimestamp(uuid);
        assertThat(extracted).isEqualTo(now);
    }

    @Test
    @DisplayName("Extracting timestamp from non-UUIDv7 or null must throw IllegalArgumentException")
    void testInvalidExtraction() {
        assertThatThrownBy(() -> UuidV7Generator.extractTimestamp(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UUID must not be null");

        UUID randomV4 = UUID.randomUUID();
        assertThatThrownBy(() -> UuidV7Generator.extractTimestamp(randomV4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected UUID version 7");
    }

    @Test
    @DisplayName("High volume generation must not produce collisions")
    void testCollisionResistance() {
        int count = 10_000;
        Set<UUID> set = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            UUID id = generator.generate();
            assertThat(id.version()).isEqualTo(7);
            set.add(id);
        }
        assertThat(set).hasSize(count);
    }

    @Test
    @DisplayName("Generator implements both GsirIdGenerator and IdGenerator interfaces")
    void testInterfaceContracts() {
        GsirIdGenerator gsirIdGen = generator;
        IdGenerator idGen = generator;

        assertThat(gsirIdGen.generate().version()).isEqualTo(7);
        assertThat(idGen.generate().version()).isEqualTo(7);
    }
}

