package com.gsir.audit;

import com.gsir.common.error.GsirException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class AccessAuditAspect {

    private final AuditService auditService;

    public AccessAuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterThrowing(pointcut = "execution(* com.gsir.projects.ProjectAccessService.requireAccess(..))", throwing = "ex")
    public void auditAccessDenied(JoinPoint joinPoint, GsirException ex) {
        Object[] args = joinPoint.getArgs();
        if (args.length >= 2 && args[0] instanceof UUID projectId && args[1] instanceof UUID userId) {
            auditService.logSecurityEvent(userId, null, "ACCESS_DENIED", "PROJECT", projectId, Map.of("reason", ex.getMessage()));
        }
    }
}
