package com.itsm.api.interceptor;

import com.itsm.core.domain.user.User;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordExpiryInterceptor implements HandlerInterceptor {

    private static final int PASSWORD_EXPIRY_DAYS = 90;

    private final UserRepository userRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String[] SKIP_PATTERNS = {
            "/api/v1/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();

        if (shouldSkip(requestUri)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Long)) {
            return true;
        }
        Long userId = (Long) principal;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return true;
        }

        if (isPasswordExpired(user.getPwdChangedAt())) {
            log.debug("Password expired for userId={}", userId);
            throw new BusinessException(ErrorCode.PASSWORD_EXPIRED);
        }

        return true;
    }

    private boolean isPasswordExpired(LocalDateTime pwdChangedAt) {
        if (pwdChangedAt == null) {
            return true;
        }
        return pwdChangedAt.plusDays(PASSWORD_EXPIRY_DAYS).isBefore(LocalDateTime.now());
    }

    private boolean shouldSkip(String requestUri) {
        for (String pattern : SKIP_PATTERNS) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }
}
