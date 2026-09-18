package com.gsir.audit;

import com.gsir.auth.dto.AuthResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class SecurityAuditAspect {

    private final AuditService auditService;
    private final com.gsir.auth.JwtService jwtService;

    public SecurityAuditAspect(AuditService auditService, com.gsir.auth.JwtService jwtService) {
        this.auditService = auditService;
        this.jwtService = jwtService;
    }

    @AfterReturning(pointcut = "execution(* com.gsir.auth.AuthService.login(..))", returning = "response")
    public void auditLogin(JoinPoint joinPoint, AuthResponse response) {
        if (response != null && response.accessToken() != null) {
            try {
                UUID userId = jwtService.extractUserId(response.accessToken());
                auditService.logSecurityEvent(userId, null, "LOGIN", "USER", userId, Map.of());
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    @AfterReturning(pointcut = "execution(* com.gsir.auth.AuthService.logoutAll(..))")
    public void auditLogoutAll(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof UUID userId) {
            auditService.logSecurityEvent(userId, null, "LOGOUT_ALL", "USER", userId, Map.of());
        }
    }
}
