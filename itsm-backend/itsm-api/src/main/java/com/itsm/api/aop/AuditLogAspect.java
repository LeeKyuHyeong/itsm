package com.itsm.api.aop;

import com.itsm.core.domain.common.AuditLog;
import com.itsm.core.repository.common.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        Long userId = getCurrentUserId();
        String ipAddress = getClientIp();
        Long targetId = extractTargetId(joinPoint, result);

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .actionType(auditable.actionType())
                .targetType(auditable.targetType())
                .targetId(targetId)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(auditLog);
    }

    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return null;
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            }
            return Long.valueOf(principal.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            return attributes.getRequest().getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractTargetId(JoinPoint joinPoint, Object result) {
        // Try: first Long argument in method params (pathVariable pattern)
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        // Fallback: try reflection on result to find an id getter
        if (result != null) {
            try {
                Method getId = result.getClass().getMethod("getId");
                Object id = getId.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (Exception ignored) {
            }

            // Try common *Id pattern methods
            for (Method method : result.getClass().getMethods()) {
                String name = method.getName();
                if (name.matches("get.+Id") && method.getParameterCount() == 0
                        && Long.class.isAssignableFrom(method.getReturnType())) {
                    try {
                        Object id = method.invoke(result);
                        if (id instanceof Long) {
                            return (Long) id;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return null;
    }
}
