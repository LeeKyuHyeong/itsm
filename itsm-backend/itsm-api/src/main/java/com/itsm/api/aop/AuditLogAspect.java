package com.itsm.api.aop;

import com.itsm.core.domain.common.AuditLog;
import com.itsm.core.dto.ApiResponse;
import com.itsm.core.repository.common.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final com.itsm.api.security.ClientIpResolver clientIpResolver;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        Long userId = getCurrentUserId();
        String ipAddress = getClientIp();
        Long targetId = extractTargetId(joinPoint, result, auditable);

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
            log.warn("감사 로그: 현재 사용자 ID 추출 실패", e);
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
            return clientIpResolver.resolve(attributes.getRequest()); // 2026-09-16 P1: 프록시 헤더 해석
        } catch (Exception e) {
            log.warn("감사 로그: 클라이언트 IP 추출 실패", e);
            return null;
        }
    }

    /**
     * 우선순위: ① targetIdProperty 지정 시 반환 DTO 의 그 getter ② 첫 Long 인자 ③ 반환 DTO 의 targetType 매칭 getter.
     * 컨트롤러 반환값은 ApiResponse 로 감싸져 있으므로 data 를 벗겨 본다.
     */
    private Long extractTargetId(JoinPoint joinPoint, Object result, Auditable auditable) {
        Object payload = unwrap(result);

        String explicit = auditable.targetIdProperty();
        if (explicit != null && !explicit.isBlank() && payload != null) {
            Long id = invokeLongGetter(payload, "get" + capitalize(explicit));
            if (id != null) {
                return id;
            }
        }

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        if (payload == null) {
            return null;
        }

        Long id = invokeLongGetter(payload, "getId");
        if (id != null) {
            return id;
        }

        List<String> candidates = Arrays.stream(payload.getClass().getMethods())
                .filter(m -> m.getName().matches("get.+Id") && m.getParameterCount() == 0
                        && Long.class.isAssignableFrom(m.getReturnType()))
                .map(Method::getName)
                .sorted() // getMethods() 순서는 비결정적 → 정렬로 결정성 확보
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }

        // ③-a targetType 을 카멜케이스로 바꾼 정확한 getter (ASSET_HW → getAssetHwId)
        String[] tokens = auditable.targetType().toLowerCase(Locale.ROOT).split("_");
        StringBuilder exact = new StringBuilder("get");
        for (String token : tokens) {
            exact.append(capitalize(token));
        }
        exact.append("Id");
        if (candidates.contains(exact.toString())) {
            return invokeLongGetter(payload, exact.toString());
        }

        // ③-b 토큰 하나라도 포함하는 getter (SERVICE_REQUEST → getRequestId). 정렬 순서로 첫 번째
        for (String candidate : candidates) {
            for (String token : tokens) {
                if (!token.isEmpty() && candidate.contains(capitalize(token))) {
                    return invokeLongGetter(payload, candidate);
                }
            }
        }

        // ③-c 그래도 없으면 정렬상 첫 번째 (과거 동작과 호환)
        return invokeLongGetter(payload, candidates.get(0));
    }

    private Object unwrap(Object result) {
        if (result instanceof ApiResponse<?> response) {
            return response.getData();
        }
        return result;
    }

    private Long invokeLongGetter(Object target, String methodName) {
        try {
            Method getter = target.getClass().getMethod(methodName);
            Object value = getter.invoke(target);
            return value instanceof Long ? (Long) value : null;
        } catch (Exception e) {
            log.debug("감사 로그: {}() 리플렉션 실패 - {}", methodName, e.getMessage());
            return null;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
