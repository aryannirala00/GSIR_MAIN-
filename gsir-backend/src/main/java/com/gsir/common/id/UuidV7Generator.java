package com.gsir.common.id;

import com.gsir.common.persistence.IdGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Canonical RFC 9562 compliant UUIDv7 generator for GSIR platform primary keys.
 * Provides time-ordered, millisecond-precision UUIDs with monotonic sorting characteristics.
 */
@Component
public class UuidV7Generator implements GsirIdGenerator, IdGenerator {

    private final SecureRandom random = new SecureRandom();

    @Override
    public UUID generate() {
        return generate(Instant.now().toEpochMilli());
    }

    /**
     * Generates a UUIDv7 using the specified Unix epoch timestamp in milliseconds.
     *
     * @param epochMillis millisecond timestamp since Unix epoch
     * @return RFC 9562 UUIDv7 instance
     */
    public UUID generate(long epochMillis) {
        // 48-bit timestamp in bits 63..16
        long high = (epochMillis & 0xFFFFFFFFFFFFL) << 16;
        // 4-bit version (0111 = 7) in bits 15..12 + 12-bit random in bits 11..0
        long randA = random.nextLong() & 0x0FFFL;
        high |= (0x7L << 12) | randA;

        // 2-bit variant (10) in bits 63..62 + 62-bit random in bits 61..0
        long randB = random.nextLong() & 0x3FFFFFFFFFFFFFFFL;
        long low = 0x8000000000000000L | randB;

        return new UUID(high, low);
    }

    /**
     * Extracts the embedded 48-bit epoch millisecond timestamp from an RFC 9562 UUIDv7.
     *
     * @param uuid the UUIDv7 to inspect
     * @return epoch millisecond timestamp
     */
    public static long extractTimestamp(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID must not be null");
        }
        if (uuid.version() != 7) {
            throw new IllegalArgumentException("Expected UUID version 7 but found version " + uuid.version());
        }
        return uuid.getMostSignificantBits() >>> 16;
    }
}

