package com.gsir.auth;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

public class JwtServiceTest {

    private final String validSecret = "this-is-a-test-only-secret-key-that-must-be-at-least-256-bits-long-for-hmac-sha-to-work-properly";
    private final long expirationMs = 900000;
    
    @Test
    void testValidJwt() {
        JwtService service = new JwtService(validSecret, expirationMs);
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        
        String token = service.generateToken(userId, sessionId);
        assertTrue(service.isTokenValid(token));
        assertEquals(userId, service.extractUserId(token));
        assertEquals(sessionId, service.extractSessionId(token));
    }
    
    @Test
    void testMissingSecretThrows() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("", expirationMs));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(null, expirationMs));
    }
    
    @Test
    void testMalformedJwtRejected() {
        JwtService service = new JwtService(validSecret, expirationMs);
        assertFalse(service.isTokenValid("this.is.not.a.jwt"));
    }
    
    @Test
    void testForgedJwtRejected() {
        JwtService service = new JwtService(validSecret, expirationMs);
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String validToken = service.generateToken(userId, sessionId);
        
        // Tamper with the payload (middle part)
        String[] parts = validToken.split("\\.");
        String forged = parts[0] + ".eyAic3ViIjogImZvcmdlZC1pZCIgfQ." + parts[2];
        
        assertFalse(service.isTokenValid(forged));
    }
    
    @Test
    void testInvalidSignatureRejected() {
        JwtService service = new JwtService(validSecret, expirationMs);
        
        // Generate a token signed with a different key
        SecretKey otherKey = Keys.hmacShaKeyFor("different-secret-key-that-is-also-256-bits-long-exactly".getBytes(StandardCharsets.UTF_8));
        String otherToken = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("sid", UUID.randomUUID().toString())
                .signWith(otherKey, Jwts.SIG.HS256)
                .compact();
                
        assertFalse(service.isTokenValid(otherToken));
    }
    
    @Test
    void testExpiredJwtRejected() {
        JwtService service = new JwtService(validSecret, -1000); // Expires in the past
        String token = service.generateToken(UUID.randomUUID(), UUID.randomUUID());
        assertFalse(service.isTokenValid(token));
    }
    
    @Test
    void testMissingSessionId() {
        JwtService service = new JwtService(validSecret, expirationMs);
        SecretKey key = Keys.hmacShaKeyFor(validSecret.getBytes(StandardCharsets.UTF_8));
        
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .signWith(key, Jwts.SIG.HS256)
                .compact();
                
        assertTrue(service.isTokenValid(token)); // Valid structure
        assertThrows(IllegalArgumentException.class, () -> service.extractSessionId(token));
    }
}


