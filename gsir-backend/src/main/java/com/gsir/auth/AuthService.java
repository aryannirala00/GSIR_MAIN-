package com.gsir.auth;

import com.gsir.auth.dto.*;
import com.gsir.common.error.CommonErrors;
import com.gsir.users.*;
import com.gsir.common.id.GsirIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GsirIdGenerator idGenerator;
    private final long refreshExpirationMs;
    private final long jwtExpirationMs;
    private final String dummyPasswordHash;

    public AuthService(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GsirIdGenerator idGenerator,
            @Value("${gsir.security.jwt.refresh-expiration-ms}") long refreshExpirationMs,
            @Value("${gsir.security.jwt.expiration-ms}") long jwtExpirationMs) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.idGenerator = idGenerator;
        this.refreshExpirationMs = refreshExpirationMs;
        this.jwtExpirationMs = jwtExpirationMs;
        // Generate a fixed dummy hash on startup for timing attack mitigation
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw CommonErrors.conflict("Email already in use");
        }

        UserEntity user = new UserEntity(
                idGenerator.generate(),
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                "ACTIVE"
        );
        userRepository.save(user);

        return createSessionAndTokens(user.getId());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        Optional<UserEntity> userOpt = userRepository.findByEmail(normalizedEmail);
        
        boolean match = false;
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            match = passwordEncoder.matches(request.password(), user.getPasswordHash());
            if (match && !"ACTIVE".equals(user.getStatus())) {
                match = false; // Treat inactive as unauthorized
            }
        } else {
            // Mitigate timing attack by performing BCrypt on a dummy hash
            passwordEncoder.matches(request.password(), dummyPasswordHash);
        }

        if (!match) {
            throw CommonErrors.unauthorized();
        }

        return createSessionAndTokens(userOpt.get().getId());
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String tokenHash = hashToken(request.refreshToken());
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> CommonErrors.unauthorized());

        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw CommonErrors.unauthorized();
        }

        // Validate session is not revoked
        if (refreshToken.getSessionId() != null) {
            Optional<SessionEntity> sessionOpt = sessionRepository.findById(refreshToken.getSessionId());
            if (sessionOpt.isPresent() && sessionOpt.get().getRevokedAt() != null) {
                throw CommonErrors.unauthorized();
            }
        }

        // Revoke the old refresh token (token rotation)
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        return createSessionAndTokens(refreshToken.getUserId());
    }

    @Transactional
    public void logout(UUID sessionId, UUID userId) {
        if (sessionId == null) return;
        
        // Revoke the specific session
        sessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getUserId().equals(userId) && session.getRevokedAt() == null) {
                session.revoke();
                sessionRepository.save(session);
            }
        });
        
        // Revoke any refresh tokens tied to this session
        refreshTokenRepository.findBySessionId(sessionId).forEach(token -> {
            if (token.getRevokedAt() == null) {
                token.revoke();
                refreshTokenRepository.save(token);
            }
        });
    }

    @Transactional
    public void logoutAll(UUID userId) {
        Instant now = Instant.now();
        sessionRepository.findByUserId(userId).forEach(session -> {
            if (session.getRevokedAt() == null) {
                session.revoke();
                sessionRepository.save(session);
            }
        });
        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            if (token.getRevokedAt() == null) {
                token.revoke();
                refreshTokenRepository.save(token);
            }
        });
    }

    private AuthResponse createSessionAndTokens(UUID userId) {
        UUID sessionId = idGenerator.generate();
        String accessToken = jwtService.generateToken(userId, sessionId);
        
        String rawRefreshToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawRefreshToken);
        
        Instant refreshExpiresAt = Instant.now().plusMillis(refreshExpirationMs);
        
        SessionEntity session = new SessionEntity(sessionId, userId, refreshExpiresAt);
        sessionRepository.save(session);
        
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(
                idGenerator.generate(),
                userId,
                tokenHash,
                refreshExpiresAt,
                sessionId
        );
        refreshTokenRepository.save(refreshTokenEntity);
        
        return new AuthResponse(accessToken, rawRefreshToken, jwtExpirationMs);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
