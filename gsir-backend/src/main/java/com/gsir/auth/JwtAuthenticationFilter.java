package com.gsir.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import com.gsir.users.SessionEntity;
import com.gsir.users.SessionRepository;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SessionRepository sessionRepository;

    public JwtAuthenticationFilter(JwtService jwtService, SessionRepository sessionRepository) {
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtService.isTokenValid(jwt)) {
                UUID userId = jwtService.extractUserId(jwt);
                UUID sessionId = jwtService.extractSessionId(jwt);
                
                Optional<SessionEntity> sessionOpt = sessionRepository.findById(sessionId);
                if (sessionOpt.isPresent()) {
                    SessionEntity session = sessionOpt.get();
                    
                    if (session.getUserId().equals(userId) && 
                        session.getRevokedAt() == null && 
                        !session.getExpiresAt().isBefore(Instant.now())) {
                        
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userId, null, Collections.emptyList());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        request.setAttribute("currentSessionId", sessionId);
                        org.slf4j.MDC.put("userId", userId.toString());
                    }
                }
            }
        } catch (Exception e) {
            // Cannot authenticate, continue filter chain
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            org.slf4j.MDC.remove("userId");
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
